package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class clear : ICommandBase {
	override val permission = Permission.ADMINISTRATOR
	override val options: List<Option> =
			listOf(
					Option("l", "limit", true, "number of message to remove").also { it.isRequired = false }
			)
	override fun invoke(args: CommandLine, event: MessageEvent): String {
		if (event.ev == null)
			return ""
		event.ch.iterableHistory.stream()
				.limit(if (args.hasOption("l")) args.getOptionValue("l").toLong() else Long.MAX_VALUE)
				.forEach { m -> m.delete().queue() }
		return ""
	}
}