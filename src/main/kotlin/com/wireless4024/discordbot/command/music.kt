package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.*
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class music : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return when (args[0]) {
			"play", "p"      -> p(args.dropFirst(), event)
			"join", "j"      -> j(event)
			"leave", "l"     -> leave(event)
			"skip", "s"      -> s(event)
			"vol", "v"       -> v(args.dropFirst(), event)
			"queue", "q"     -> queue(args.dropFirst(), event)
			"clear", "c"     -> clear(event)
			"pause"          -> pause(event)
			"repeat", "r"    -> repeat(args.dropFirst(), event)
			"remove", "d"    -> remove(args.dropFirst(), event)
			"previous", "pv" -> previous(event)
			"forward", "fw"  -> forward(args.dropFirst(), event)
			"backward", "bw" -> backward(args.dropFirst(), event)
			"seek"           -> seek(args.dropFirst(), event)
			"now"            -> now(event)
			else             -> ""
		}
	}

	@Command
	fun p(args: CommandLine, event: MessageEvent): String =
		event.ensureVoiceConnected { event.musicController.queue(args, event);"" }

	@Command
	fun j(event: MessageEvent): String =
		event.ensureVoiceConnected { "connecting to '${event.musicController.join(event)}'" }

	@Command
	fun s(event: MessageEvent): String {
		event.ensureVoiceConnected()
		val playing = event.musicController.skip()
		return if (playing != null) "now playing $playing" else "player stopped"
	}

	@Command
	fun v(args: CommandLine, event: MessageEvent): String {
		event.ensureVoiceConnected()
		return "volume is ${event.musicController.volume(args[0]?.toIntOrNull() ?: 0)}"
	}

	@Command
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
	fun bassboost(args: CommandLine, event: MessageEvent): String {
		event.ensureVoiceConnected()
		return "bass boost : ${if (event.musicController.bassBoost(args[0]?.parseInt())) "on" else "off"}"
	}

	@Command
	fun leave(event: MessageEvent): String {
		event.ensureVoiceConnected()
		return "I'm leaving ${event.musicController.leave()}"
	}

	@Command
	fun clear(event: MessageEvent): String {
		event.musicController.clear()
		return "cleared playlist"
	}

	@Command
	fun queue(args: CommandLine, event: MessageEvent): MessageEmbed {
		return event.musicController.listAsEmbed(args.args.getOrNull(0)?.toIntOrNull() ?: 1)
	}

	@Command
	fun pause(event: MessageEvent) = if (event.musicController.pause(event)) "resume playing" else "player paused"

	@Command
	fun repeat(args: CommandLine, event: MessageEvent): String {
		return "repeat: ${event.musicController.repeat(args[0] ?: "")}"
	}

	@Command
	fun remove(args: CommandLine, event: MessageEvent): String {
		return "removed track : ${event.musicController.removeQueue(
			args.args.getOrNull(0)?.toIntOrNull()
				?: throw CommandError("invalid index")
		).info.title}"
	}

	@Command
	fun previous(event: MessageEvent): String {
		return event.musicController.previous(event.voiceChannel)?.let { "now playing : $it" } ?: ""
	}

	@Command
	fun forward(args: CommandLine, event: MessageEvent): String {
		return "now playing at ${Utils.toReadableFormatTime(
			event.musicController.forward(
				(args[0]?.toIntOrNull()
					?: 5) * 1000
			)
		)}"
	}

	@Command
	fun backward(args: CommandLine, event: MessageEvent): String {
		return "now playing at ${Utils.toReadableFormatTime(
			event.musicController.back(
				(args[0]?.toIntOrNull()
					?: 5) * 1000
			)
		)}"
	}

	@Command
	fun seek(args: CommandLine, event: MessageEvent): String {
		return "now playing at ${Utils.toReadableFormatTime(
			event.musicController.seek(
				(args[0]?.toLongOrNull()
					?: 0) * 1000
			)
		)}"
	}

	override val options: List<Option> = listOf()

	override val permission: Int = Permission.ANY
}