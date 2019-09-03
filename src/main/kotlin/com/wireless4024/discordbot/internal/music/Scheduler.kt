package com.wireless4024.discordbot.internal.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import java.util.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class Scheduler(private val player: AudioPlayer,
                private val parent: Controller) : AudioEventAdapter() {

	private val queue: BlockingDeque<AudioTrack>
	private val boxMessage: AtomicReference<Message>
	private val repeat: AtomicBoolean

	init {
		this.queue = LinkedBlockingDeque()
		this.boxMessage = AtomicReference()
		this.repeat = AtomicBoolean()
	}

	fun addToQueue(vararg audioTrack: AudioTrack) {
		for (track in audioTrack)
			queue.addLast(track)

		startNextTrack(true)
	}

	fun drainQueue(): Array<AudioTrack> {
		val drainedQueue = ArrayList<AudioTrack>()
		queue.drainTo(drainedQueue)
		return drainedQueue.toTypedArray()
	}

	fun playNow(audioTrack: AudioTrack, clearQueue: Boolean) {
		if (clearQueue) queue.clear()
		queue.addFirst(audioTrack)
		startNextTrack(false)
	}

	fun size() = queue.size

	/**
	 * skip current track
	 */
	fun skip() = startNextTrack(false)

	fun clear() = queue.clear()

	val queues
		get() = queue.toTypedArray()

	val queueDuation
		get() = kotlin.run {
			var duration = 0L
			for (a in queue)
				duration += a.duration
			duration
		}

	fun repeat() = repeat.set(repeat.get())

	private fun startNextTrack(noInterrupt: Boolean, lastTrack: AudioTrack? = null): String? {
		if (!repeat.get() || lastTrack == null) {
			if (queue.isNotEmpty() && queue.first != null) {
				if (!player.startTrack(queue.first, noInterrupt))
					queue.addFirst(queue.first)
				queue.removeFirst()
			} else {
				player.stopTrack()
				GlobalScope.launch {
					delay(30000)
					if (queue.isEmpty() || queue.first == null)
						parent.leave()
				}
				// messageDispatcher.sendMessage("Queue finished.")
			}
		} else player.startTrack(lastTrack, true)
		return player.playingTrack?.info?.title
	}

	override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {

	}

	override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason) {
		if (repeat.get())
			startNextTrack(true, track!!.makeClone())
		else if (endReason.mayStartNext) {
			startNextTrack(true)
			// messageDispatcher.sendMessage(String.format("Track %s finished.", track!!.info.title))
		}
	}

	override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack, thresholdMs: Long) {
		// messageDispatcher.sendMessage(String.format("Track %s got stuck, skipping.", track.info.title))

		startNextTrack(false)
	}

	override fun onPlayerResume(player: AudioPlayer?) {

	}

	override fun onPlayerPause(player: AudioPlayer?) {

	}
}