package com.wireless4024.discordbot.command.string

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class sort : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): String {
		if (!args.hasOption("desc"))
			return if (args.args.size == 1) args.args[0].toList().sorted().joinToString("")
			else args.args.sorted().joinToString("\n")
		return if (args.args.size == 1) args.args[0].toList().sorted().reversed().joinToString("")
		else args.args.sorted().reversed().joinToString("\n")
	}

	override val options: List<Option> = listOf(Option("d", "desc", false, "descending").also {
		it.isRequired = false
	})
	override val permission: Int = Permission.ANY
}