package com.wireless4024.discordbot.command.string

import com.wireless4024.discordbot.internal.CommandError
import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.parseInt
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class randstr : ICommandBase {
	companion object {
		@JvmStatic
		val endWithNum = Regex("(?: )\\d+\$")
	}

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		if (args.args.isEmpty())
			throw CommandError("use case randstr <charlist> [limit]")
		val charlist = event.msg
		val charlen = charlist.length
		val limit = endWithNum.find(charlist)?.value?.trim()?.parseInt() ?: charlen
		val rnd = java.util.Random()
		if (limit == charlen)
			return String(charlist.toCharArray().toMutableList().also { it.shuffle(rnd) }.toCharArray())
		val chars = CharArray(limit)
		for (i in chars.indices)
			chars[i] = charlist[rnd.nextInt(charlen)]
		return String(chars)
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
}