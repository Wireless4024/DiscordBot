package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.*
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class music : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return when (args[0]) {
			"play", "p"  -> p(args.dropFirst(), event)
			"join", "j"  -> j(args.dropFirst(), event)
			"leave", "l" -> leave(args.dropFirst(), event)
			"skip", "s"  -> s(args.dropFirst(), event)
			"vol", "v"   -> v(args.dropFirst(), event)
			"queue", "q" -> queue(args.dropFirst(), event)
			"clear", "c" -> clear(args.dropFirst(), event)
			else         -> ""
		}
	}

	@Command
	fun p(args: CommandLine, event: MessageEvent): String =
			event.ensureVoiceConnected { event.musicController.queue(args, event);"" }

	@Command
	fun j(args: CommandLine, event: MessageEvent): String =
			event.ensureVoiceConnected { "connecting to '${event.musicController.join(event)}'" }

	@Command
	fun s(args: CommandLine, event: MessageEvent): String {
		event.ensureVoiceConnected()
		val playing = event.musicController.skip(event)
		return if (playing != null) "now playing $playing" else "player stopped"
	}

	@Command
	fun v(args: CommandLine, event: MessageEvent): String {
		event.ensureVoiceConnected()
		return "volume is ${event.musicController.volume(args[0]?.toIntOrNull() ?: 0)}"
	}

	@Command
	fun leave(args: CommandLine, event: MessageEvent): String {
		event.ensureVoiceConnected()
		return "I'm leaving ${event.musicController.leave(event)}"
	}

	@Command
	fun clear(args: CommandLine, event: MessageEvent): String {
		event.musicController.clear()
		return "cleared playlist"
	}

	@Command
	fun queue(args: CommandLine, event: MessageEvent): MessageEmbed {
		return event.musicController.listAsEmbed(
				event, args.args.getOrNull(0)?.toIntOrNull() ?: 1
		)
	}

	@Command
	fun pause(args: CommandLine, event: MessageEvent): String {
		return if (event.musicController.pause()) "paused " else "resume"
	}

	override val options: List<Option> = listOf()

	override val permission: Int = Permission.ANY
}