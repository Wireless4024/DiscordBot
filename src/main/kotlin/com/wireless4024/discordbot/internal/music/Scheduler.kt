package com.wireless4024.discordbot.internal.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import com.wireless4024.discordbot.internal.CommandError
import com.wireless4024.discordbot.internal.music.Repeat.*
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus
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

	internal fun getQueue() = queue.toMutableList().also {
		if (player.playingTrack != null)
			it.add(0, player.playingTrack)
	}.toTypedArray()

	internal fun loadQueue(data: Array<AudioTrack>) = data.forEach { queue.addLast(it) }.also {
		startNextTrack(true)
	}

	val queueDuation
		get() = kotlin.run {
			if (queue.isEmpty())
				return@run 0L
			var duration = 0L
			for (a in queue)
				duration += a.duration
			duration
		}

	val queueRemaining
		get() = if ((queue.size != 0 || player.playingTrack != null) && repeat != NO) Long.MAX_VALUE
		else queueDuation + (player.playingTrack?.duration ?: 0)

	fun repeat(kw: String = ""): String {
		if (kw.startsWith("ge"))
			return repeat.name
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

		val am = parent.parent.audioManager
		player.isPaused = (am.connectedChannel?.members?.size ?: 2) < 2 && !am.isConnected

		if (repeat != SINGLE || lastTrack == null) {
			if (repeat == ALL && lastTrack != null)
				queue.addLast(lastTrack.makeClone())
			if (queue.isNotEmpty() && queue.first != null) {
				if (player.startTrack(queue.first, noInterrupt))
					queue.removeFirst()
			} else {
				player.stopTrack()
				parent.attemptToLeave()
				// messageDispatcher.sendMessage("Queue finished.")
			}
		} else player.startTrack(lastTrack.makeClone(), noInterrupt)
		if (player.isPaused)
			parent.attemptToLeave()
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