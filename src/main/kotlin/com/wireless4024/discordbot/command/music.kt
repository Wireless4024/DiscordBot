package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.Utils
import com.wireless4024.discordbot.internal.*
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.VoiceChannel
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class music : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return when (args[0]) {
			"play", "p"      -> p(event.dropFirst())
			"join", "j"      -> j(event.dropFirst())
			"leave", "l"     -> leave(event.dropFirst())
			"skip", "s"      -> s(event.dropFirst())
			"vol", "v"       -> v(event.dropFirst())
			"queue", "q"     -> queue(event.dropFirst())
			"clear", "c"     -> clear(event.dropFirst())
			"pause"          -> pause(event.dropFirst())
			"repeat", "r"    -> repeat(event.dropFirst())
			"remove", "d"    -> remove(event.dropFirst())
			"previous", "pv" -> previous(event.dropFirst())
			"forward", "fw"  -> forward(event.dropFirst())
			"backward", "bw" -> backward(event.dropFirst())
			"seek"           -> seek(event.dropFirst())
			"now"            -> now(event.dropFirst())
			"bassbost", "bb" -> bassboost(event.dropFirst())
			else             -> ""
		}
	}

	@Command
	@SkipArguments
	fun p(event: MessageEvent): String =
		event.ensureVoiceConnected { event.musicController.queue(event);"" }

	@Command
	@SkipArguments
	fun ytsearch(event: MessageEvent) = event.musicController.search(event.msg, event)

	@Command
	@SkipArguments
	fun j(event: MessageEvent): String =
		event.ensureVoiceConnected { "connecting to '${event.musicController.join(event)}'" }

	@Command
	@SkipArguments
	fun s(event: MessageEvent): String {
		event.ensureVoiceConnected()
		val playing = event.musicController.skip()
		return if (playing != null) "now playing $playing" else "player stopped"
	}

	@Command
	@SkipArguments
	fun v(event: MessageEvent): String {
		event.ensureVoiceConnected()
		return "volume is ${event.musicController.volume(event.msg.toIntOrNull() ?: 0)}"
	}

	@Command
	@SkipArguments
	fun now(event: MessageEvent): String {
		val player = event.musicController.player().playingTrack ?: return "not playing"
		val playingInfo = player.info
		return """now playing: `${playingInfo.title}` 
url: ${playingInfo.uri} 
duration: `${Utils.toReadableFormatTime(playingInfo.length)}`
played: `${Utils.toReadableFormatTime(player.position)}`
remaining: `${Utils.toReadableFormatTime(playingInfo.length - player.position)}`"""
	}

	@Command
	@SkipArguments
	fun bassboost(event: MessageEvent): String {
		event.ensureVoiceConnected()
		return "bass boost : ${if (event.musicController.bassBoost(event.msg.parseInt())) "on" else "off"}"
	}

	@Command
	@SkipArguments
	fun leave(event: MessageEvent): String {
		event.ensureVoiceConnected()
		return "I'm leaving ${event.musicController.leave()}"
	}

	@Command
	@SkipArguments
	fun clear(event: MessageEvent): String {
		event.musicController.clear()
		return "cleared playlist"
	}

	@Command
	@SkipArguments
	fun queue(event: MessageEvent): MessageEmbed {
		return event.musicController.listAsEmbed(event.msg.toIntOrNull() ?: 1)
	}

	@Command
	@SkipArguments
	fun pause(event: MessageEvent) = if (event.musicController.pause(event)) "resume playing" else "player paused"

	@Command
	@SkipArguments
	fun repeat(event: MessageEvent): String {
		return "repeat: ${event.musicController.repeat(event.msg)}"
	}

	@Command
	@SkipArguments
	fun remove(event: MessageEvent): String {
		return "removed track : ${event.musicController.removeQueue(
			event.msg.toIntOrNull() ?: throw CommandError("invalid index")
		).info.title}"
	}

	@Command
	@SkipArguments
	fun previous(event: MessageEvent): String {
		return event.musicController.previous(event.voiceChannel as VoiceChannel)?.let { "now playing : $it" } ?: ""
	}

	@Command
	@SkipArguments
	fun forward(event: MessageEvent): String {
		return "now playing at ${
            Utils.toReadableFormatTime(
			event.musicController.forward(
				(event.msg.toIntOrNull() ?: 5) * 1000
			)
		)}"
	}

	@Command
	@SkipArguments
	fun backward(event: MessageEvent): String {
		return "now playing at ${
            Utils.toReadableFormatTime(
			event.musicController.back((event.msg.toIntOrNull() ?: 5) * 1000)
		)}"
	}

	@Command
	@SkipArguments
	fun seek(event: MessageEvent): String {
		return "now playing at ${
            Utils.toReadableFormatTime(
			event.musicController.seek(
				(event.msg.toLongOrNull() ?: 0) * 1000
			)
		)}"
	}

	override val options: List<Option> = listOf()

	override val permission: Int = Permission.ANY
}