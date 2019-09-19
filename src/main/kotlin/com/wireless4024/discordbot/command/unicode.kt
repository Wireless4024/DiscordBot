package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.parseInt
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class unicode : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		if (args.hasOption("c"))
			return args.args.asSequence()
				.map { it.parseInt() }.filterNotNull().map { it.toChar() }
				.joinToString(", ", "[", "]")
		if (args.hasOption("x"))
			return args.args.joinToString(" ").replace(Regex("-+[^ ]+"), "").toCharArray()
				.map { it.toInt().toString(16) }
		if (args.hasOption("c"))
			return args.args.joinToString(" ").replace(Regex("-+[^ ]+"), "").toCharArray()
				.map { it.toInt().toString(2) }
		return event.msg.toCharArray().map { it.toInt() }
	}

	override val options: List<Option> = listOf(Option("x", "hex", false, "return hex string").also {
		it.isRequired = false
	}, Option("b", "binary", false, "return binary string").also {
		it.isRequired = false
	}, Option("c", "char", false, "return character from number").also {
		it.isRequired = false
	})
	override val permission: Int = Permission.ANY
}