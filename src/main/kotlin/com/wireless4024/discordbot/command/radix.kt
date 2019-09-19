package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.CommandError
import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.get
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class radix : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return try {
			if (args.args.isEmpty())
				throw CommandError("use case radix <input:to> <number>")
			else if (args.args.size == 1) {
				val arg = args[0]!!
				return when {
					arg.startsWith("0b", true) -> arg.drop(2).toBigIntegerOrNull(2) ?: "invalid binary"
					arg.startsWith("0x", true) -> arg.drop(2).toBigIntegerOrNull(16) ?: "invalid hexadecimal"
					arg.startsWith("0", true)  -> arg.drop(1).toBigIntegerOrNull(8) ?: "invalid octal"
					else                       -> "use case radix <0bNumber,0Number,0xNumber>"
				}
			}
			val (from, to) = args[0]!!.split(':').map { it.toIntOrNull() }
				.run {
					if (this@run.size == 1) arrayOf(10, this@run[0]!!)
					else arrayOf(this.getOrNull(0) ?: 10, this.getOrNull(1) ?: 10)
				}
			val number = args[1]!!.toBigIntegerOrNull(from)!!
			number.toString(to)
		} catch (e: NullPointerException) {
			"use case radix <input:to> <number>"
		}
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
}