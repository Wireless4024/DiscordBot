package com.wireless4024.discordbot.internal.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.wireless4024.discordbot.internal.CommandError
import com.wireless4024.discordbot.internal.ConfigurationCache
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.managers.AudioManager
import org.apache.commons.cli.CommandLine
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class Controller(val parent: ConfigurationCache) {
	private var player: AudioPlayer
	private var manager: AudioPlayerManager = parent.audioPlayerManager
	private val scheduler: Scheduler

	init {
		player = manager.createPlayer()
		parent.audioSendHandler = AudioHandler(player)

		scheduler = Scheduler(player, this)
		player.addListener(scheduler)
	}

	private fun add(message: Message, identifier: String) {
		//addTrack(message, identifier, false)
	}

	fun now(msgEV: MessageEvent) {
		//addTrack(message, identifier, true)

		connect(parent.audioManager, msgEV.member.voiceState?.channel)
	}

	fun queue(args: CommandLine, msgEV: MessageEvent) {
		val text = args.args.joinToString(" ").trim()
		if (Utils.urlExisted(text))
			addTrack(text, msgEV)
		else
			addTrack(if (text.startsWith("ytsearch:")) text else "ytsearch:$text", msgEV, !text.startsWith("ytsearch:"))
	}

	fun join(msgEV: MessageEvent) = connect(parent.audioManager, msgEV.member.voiceState?.channel, true)

	fun leave(msgEV: MessageEvent? = null) =
			with(parent.audioManager) { (this.connectedChannel?.name).also { scheduler.clear();this.closeAudioConnection() } }
			?: throw CommandError("wait dude I i can't leave I MOST BE IN VOICE CHANNEL TO USE THIS COMMAND!")

	fun skip(msgEV: MessageEvent) = scheduler.skip()

	fun clear() = scheduler.clear()

	fun volume(vol: Int): Int {
		if (vol <= 0)
			player.volume += vol
		else
			player.volume = vol
		return player.volume
	}

	fun listAsEmbed(msgEV: MessageEvent, page: Int = 1): MessageEmbed {
		val queue = scheduler.queues
		return EmbedBuilder().also {
			it.setTitle("Song queue | page $page")
			it.setDescription("${queue.size} song in queue | duration ${Utils.toReadableFormatTime(scheduler.queueDuation)} remaining")
			it.setColor(Color.GREEN)
			for (i in queue.safePartition(page))
				it.addField(i.info.title, "duration : " + Utils.toReadableFormatTime(i.duration), false)
		}.build()
	}

	private inline fun <reified T> Array<T>.safePartition(page: Int, size: Int = 10): Array<T> {
		val min = (page - 1) * size
		val end = min(min + size, this.size)
		if (min >= this.size)
			return emptyArray()
		return this.sliceArray(min until end)
	}

	private fun forward(message: Message, duration: Int) {
		playing {
			it.position = it.position + duration
		}
	}

	private fun back(message: Message, duration: Int) {
		playing {
			it.position = max(0, it.position - duration)
		}
	}

	fun pause(): Boolean {
		return player.isPaused.also { player.isPaused = !it }
	}

	val duration
		get() = playing { it }?.duration ?: 0

	fun seek(message: Message, position: Long) {
		playing {
			it.position = position
		}
	}

	private fun pos(message: Message) {
		playing {
			message.channel.sendMessage("Position is " + it.position).queue()
		}
	}

	private fun leave(message: Message) {
		parent.closeAudioConnection()
	}

	/**
	 * @param pickfirst Boolean if search result ticked as playlist should player add song as single track
	 */
	private fun addTrack(url: String, event: MessageEvent, pickfirst: Boolean = false) {
		manager.loadItemOrdered(this, url, object : AudioLoadResultHandler {
			override fun trackLoaded(track: AudioTrack) {
				connect(parent.audioManager, event.member.voiceState?.channel)

				event.reply("now playing: ${track.info.title} (length ${Utils.toReadableFormatTime(track.duration)})")
				scheduler.addToQueue(track)
			}

			override fun playlistLoaded(playlist: AudioPlaylist) {
				connect(parent.audioManager, event.member.voiceState?.channel)

				if (pickfirst)
					return this.trackLoaded(playlist.selectedTrack ?: playlist.tracks[0])

				val tracks = playlist.tracks
				var len = 0
				var duration = 0L
				tracks.slice(
					(if (playlist.selectedTrack == null) 0 else tracks.indexOf(
						playlist.selectedTrack
					)) until tracks.size
				).forEach { ++len;duration += it.duration;scheduler.addToQueue(it) }.also {
					event.reply = "added $len tracks duration ${Utils.toReadableFormatTime(duration)}"
				}
			}

			override fun noMatches() {
				event.reply("Nothing found for $url")
			}

			override fun loadFailed(throwable: FriendlyException) {
				event.reply("Failed with message: " + throwable.message + " (" + throwable.javaClass.simpleName + ")")
			}
		})
	}

	private fun <T> playing(operation: ((AudioTrack) -> T?)): T? = with(player.playingTrack) { operation(this) }

	private fun connect(audioManager: AudioManager, voiceChannel: VoiceChannel?, force: Boolean = false): String {
		if (voiceChannel == null)
			throw CommandError("You must be in voice channel to use command")
		if (force || !audioManager.isConnected && !audioManager.isAttemptingToConnect)
			audioManager.openAudioConnection(voiceChannel)
		return voiceChannel.name
	}
}