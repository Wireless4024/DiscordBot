package com.wireless4024.discordbot.command.string

import com.wireless4024.discordbot.internal.*
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

@SkipArguments
class randstr : ICommandBase {

	companion object {
		@JvmStatic
		val endWithNum = Regex("(?: )\\d+\$")
	}

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		if (event.msg.isEmpty())
			throw CommandError("use case randstr <charlist> [limit]")
		val lim = endWithNum.find(event.msg)
		val limit = lim?.value?.trim()?.parseInt() ?: event.msg.length
		val charlist = if (lim != null) event.msg.removeRange(lim.range).trim() else event.msg
		val charlen = charlist.length
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