package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class say : ICommandBase {
	override val permission = Permission.ADMINISTRATOR
	override val options: List<Option> = listOf()
	override fun invoke(args: CommandLine, event: MessageEvent): String {
		return args.args.joinToString(" ").trim()
	}
}