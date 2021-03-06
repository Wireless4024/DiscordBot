package com.wireless4024.discordbot.command.string

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.SkipArguments
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

@SkipArguments
class reverse : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return event.msg.reversed()
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
}