package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.SkipArguments
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

@SkipArguments
class prefix : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		if (event.msg.isNotBlank()) {
			event.configuration.prefix = event.msg
			return "changed prefix to ${event.configuration.prefix}"
		}
		return event.configuration.prefix
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.DEV or Permission.ADMINISTRATOR or Permission.OWNER
}