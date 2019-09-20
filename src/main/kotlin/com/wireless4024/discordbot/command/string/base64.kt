package com.wireless4024.discordbot.command.string

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.parseInt
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.util.*

class base64 : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		val isNumberic =
			args.args.map { it.parseInt() != null }.reduce { a, b -> a == b && a }
		val raw = event.msg.replace(Regex(" -+[^ ]+"), "")
		val dest =
			if (isNumberic) args.args.asSequence().map { it.parseInt()!!.toByte() }.toList().toByteArray() else raw.toByteArray()
		return Base64.getEncoder().encodeToString(dest)
	}

	override val options: List<Option> = listOf(Option("d", "decode", false, "decode base64").also {
		it.isRequired = false
	})
	override val permission: Int = Permission.ANY
}