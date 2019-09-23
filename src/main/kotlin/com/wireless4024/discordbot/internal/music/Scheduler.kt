package com.wireless4024.discordbot.internal.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.wireless4024.discordbot.internal.CommandError
import com.wireless4024.discordbot.internal.Property
import com.wireless4024.discordbot.internal.music.Repeat.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.Message
import java.util.*
import java.util.concurrent.BlockingDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicReference

class Scheduler(
	private val player: AudioPlayer,
	private val parent: Controller
) : AudioEventAdapter() {

	private var queue: BlockingDeque<AudioTrack>
	private val boxMessage: AtomicReference<Message>
	private var repeat: Repeat
	private var lastTrack: AudioTrack? = null

	init {
		this.queue = LinkedBlockingDeque()
		this.boxMessage = AtomicReference()
		this.repeat = NO
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

	fun playNow(audioTrack: AudioTrack, clearQueue: Boolean): String? {
		if (clearQueue) queue.clear()
		queue.addFirst(audioTrack.makeClone())
		return startNextTrack(false)
	}

	fun size() = queue.size
	/**
	 * skip current track
	 */
	fun skip() = startNextTrack(false, player.playingTrack)

	fun previous(): String? {
		if (lastTrack == null)
			throw CommandError("player doesn't have last played track")
		if (player.playingTrack != null)
			queue.addFirst(player.playingTrack)
		return playNow(lastTrack!!, false).also { lastTrack = null }
	}

	fun clear() = queue.clear()

	val queues
		get() = queue.toTypedArray()

	val queueDuation
		get() = kotlin.run {
			if (repeat != NO)
				return@run Long.MAX_VALUE
			var duration = 0L
			for (a in queue)
				duration += a.duration
			duration
		}

	fun repeat(kw: String = ""): String {
		repeat = when {
			kw.startsWith("on", false) || kw.startsWith("si", false) -> SINGLE
			kw.startsWith("al", false) || kw.startsWith("fu", false) -> ALL
			else                                                     -> if (repeat == NO) ALL else NO
		}
		return repeat.name
	}

	fun remove(pos: Int): AudioTrack {
		if (pos < 1 || pos > queue.size)
			throw CommandError("position out of bound")
		var elem: AudioTrack
		queue = LinkedBlockingDeque(queue.toMutableList().also { elem = it.removeAt(pos - 1) })
		return elem
	}

	private fun startNextTrack(noInterrupt: Boolean, lastTrack: AudioTrack? = null): String? {
		lastTrack?.run { this@Scheduler.lastTrack = this }
		if (repeat != SINGLE || lastTrack == null) {
			if (queue.isNotEmpty() && queue.first != null) {
				if (player.startTrack(queue.first, noInterrupt))
					queue.removeFirst()
				if (repeat == ALL && lastTrack != null)
					queue.addLast(lastTrack.makeClone())
			} else {
				player.stopTrack()
				GlobalScope.launch {
					delay(Property.BASE_SLEEP_DELAY_MILLI)
					if (player.playingTrack == null && (queue.isEmpty() || queue.first == null))
						parent.leave()
				}
				// messageDispatcher.sendMessage("Queue finished.")
			}
		} else player.startTrack(lastTrack.makeClone(), noInterrupt)
		return player.playingTrack?.info?.title
	}

	override fun onTrackStart(player: AudioPlayer?, track: AudioTrack?) {

	}

	override fun onTrackEnd(player: AudioPlayer?, track: AudioTrack?, endReason: AudioTrackEndReason) {
		if (endReason.mayStartNext) {
			startNextTrack(true, track)
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

enum class Repeat {
	NO,
	SINGLE,
	ALL
}