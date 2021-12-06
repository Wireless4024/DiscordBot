package com.wireless4024.discordbot.command.util

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Utils.Companion.scheduleexecutor
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.util.concurrent.TimeUnit

class Notify : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		val (interval, message: String) = event.msg.split(' ', limit = 2)
		val wait = interval.toLongOrNull() ?: 0
		if (wait == 0L) return "Invalid interval"
		scheduleexecutor.schedule({
			event.reply = message.ifBlank { "Hey!! fucker ${event.user.asTag}" }
		}, wait, TimeUnit.MINUTES)
		return "schedule to notify in next $wait minutes"
	}

	override val options: List<Option> = listOf()
	override val permission: Int = 0
}