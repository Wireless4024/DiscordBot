package com.wireless4024.discordbot.internal.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.wireless4024.discordbot.internal.CommandError
import com.wireless4024.discordbot.internal.ConfigurationCache
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Utils
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.managers.AudioManager
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.max

class Controller(
		val state: GuildContext,
		val parent: ConfigurationCache,
		private var player: AudioPlayer = DefaultAudioPlayer.createPlayer(),
		private var outputChannel: AtomicReference<TextChannel>,
		private var scheduler: Scheduler
) {

	companion object {
		val DefaultAudioPlayer = DefaultAudioPlayerManager().also {
			it.registerSourceManager(YoutubeAudioSourceManager())
			it.registerSourceManager(SoundCloudAudioSourceManager())
			it.registerSourceManager(BandcampAudioSourceManager())
			it.registerSourceManager(VimeoAudioSourceManager())
			it.registerSourceManager(TwitchStreamAudioSourceManager())
			it.registerSourceManager(BeamAudioSourceManager())
			it.registerSourceManager(HttpAudioSourceManager())
			it.registerSourceManager(LocalAudioSourceManager())
		}
	}

	init {
		this.player = DefaultAudioPlayer.createPlayer()

		parent.guild.audioManager.sendingHandler = AudioHandler(player)
		outputChannel = AtomicReference()
		scheduler = Scheduler(player, Utils.scheduleexecutor)
		player.addListener(scheduler)
	}

	private fun add(message: Message, identifier: String) {
		//addTrack(message, identifier, false)
	}

	private fun now(message: Message, identifier: String) {
		//addTrack(message, identifier, true)
	}

	private fun play(msgEV: MessageEvent) {
		//addTrack(message, identifier, true)
		connect(parent.audioManager, msgEV.member.voiceState?.channel)
	}

	private fun skip(msgEV: MessageEvent) {
		scheduler.skip()
	}

	private fun forward(message: Message, duration: Int) {
		thisTrack {
			it.position = it.position + duration
		}
	}

	private fun back(message: Message, duration: Int) {
		thisTrack {
			it.position = max(0, it.position - duration)
		}
	}

	private fun pause() {
		player.isPaused = !player.isPaused
	}

	private fun duration(message: Message) {
		thisTrack {
			message.channel.sendMessage("Duration is " + it.duration).queue()
		}
	}

	private fun seek(message: Message, position: Long) {
		thisTrack {
			it.position = position
		}
	}

	private fun pos(message: Message) {
		thisTrack {
			message.channel.sendMessage("Position is " + it.position).queue()
		}
	}

	private fun leave(message: Message) {
		parent.closeAudioConnection()
	}

	/*private fun addTrack(message: Message, identifier: String, now: Boolean) {
		outputChannel.set(message.channel as TextChannel)
		manager.loadItemOrdered(this, identifier, object : AudioLoadResultHandler {
			override fun trackLoaded(track: AudioTrack) {
				connectToFirstVoiceChannel(guild.getAudioManager())
				message.channel.sendMessage("Starting now: " + track.info.title + " (length " + track.duration + ")")
					.queue()
				if (now) {
					scheduler.playNow(track, true)
				} else {
					scheduler.addToQueue(track)
				}
			}

			override fun playlistLoaded(playlist: AudioPlaylist) {
				val tracks = playlist.tracks
				message.channel.sendMessage("Loaded playlist: " + playlist.name + " (" + tracks.size + ")").queue()
				connectToFirstVoiceChannel(guild.getAudioManager())
				var selected: AudioTrack? = playlist.selectedTrack
				if (selected != null) {
					message.channel.sendMessage("Selected track from playlist: " + selected.info.title).queue()
				} else {
					selected = tracks[0]
					message.channel.sendMessage("Added first track from playlist: " + selected!!.info.title).queue()
				}
				if (now) {
					scheduler.playNow(selected, true)
				} else {
					scheduler.addToQueue(selected)
				}
				for (i in 0 until Math.min(10, playlist.tracks.size)) {
					if (tracks[i] !== selected) {
						scheduler.addToQueue(tracks[i])
					}
				}
			}

			override fun noMatches() {
				message.channel.sendMessage("Nothing found for $identifier").queue()
			}

			override fun loadFailed(throwable: FriendlyException) {
				message.channel.sendMessage("Failed with message: " + throwable.message + " (" + throwable.javaClass.simpleName + ")")
					.queue()
			}
		})
	}*/

	private fun thisTrack(operation: ((AudioTrack) -> Unit)) = with(player.playingTrack) { operation(this) }

	private fun connect(audioManager: AudioManager, voiceChannel: VoiceChannel?) {
		if (voiceChannel == null)
			throw CommandError("You must be in voice channel to use command")
		if (!audioManager.isConnected && !audioManager.isAttemptingToConnect)
			audioManager.openAudioConnection(voiceChannel)
	}
}