package com.wireless4024.discordbot.command.util

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Utils.Companion.scheduleexecutor
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.util.concurrent.TimeUnit

class n : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		val wait = event.msg.toLongOrNull() ?: 0
		val msg = "Wake up trainer we have legendary to catch ${
			event.guild.getEmotesByName("DoggoAngry", true).first().asMention
		}${event.guild.getEmotesByName("pika", true).first().asMention}"

		if (wait == 0L) return msg
		scheduleexecutor.schedule({
			event.reply = msg
		}, wait, TimeUnit.MINUTES)
		return "schedule to notify in next $wait minutes"
	}

	override val options: List<Option> = listOf()
	override val permission: Int = 0
}