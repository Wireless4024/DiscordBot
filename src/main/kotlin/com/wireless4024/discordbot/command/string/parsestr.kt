package com.wireless4024.discordbot.command.string

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.SkipArguments
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

@SkipArguments
class parsestr : ICommandBase {

	companion object {
		@JvmStatic val UTF16 = Regex("(?<!\\\\)\\\\(?!\\\\)u[0-9a-fA-F]{4}")
		@JvmStatic val UTF8 = Regex("((?<!\\\\)\\\\(?!\\\\)x|(?<!\\\\)%(?!\\\\))[0-9a-fA-F]{2}")
		@JvmStatic fun escape(str: String): String {
			return str
				.replace("\\r\\n", "\r\n")
				.replace("\\r", "\r")
				.replace("\\n", "\n")
				.replace("\\f", "\u000c")
				.replace("\\t", "\t")
		}

		@JvmStatic fun parse(str: String): String {
			var result = UTF16.replace(str) { it.value.drop(2).toInt(16).toChar().toString() }
			val cursor = arrayOf(0, -1) // [start, end]
			val byteRange: MutableList<Pair<IntRange, String>> = mutableListOf()
			UTF8.findAll(result).let {
				it.forEach { item ->
					with(item.range) {
						if (cursor[1] != first - 1) { // if last index is not stick with current position add found range into list
							if (cursor[1] != -1)
								byteRange.add(cursor[0]..cursor[1] to "") // add current range into list
							cursor[0] = first
						}
						cursor[1] = last
					}
				}
				if (cursor[1] != -1)
					byteRange.add(cursor[0]..cursor[1] to "")
			}
			byteRange.forEachIndexed() { i, it ->
				val bytes = UTF8.findAll(result.substring(it.first)).map { match ->
					match.value.drop(if (match.value.startsWith('%')) 1 else 2).toInt(16).toByte()
				}.toList().toByteArray()
				byteRange[i] = it.first to String(bytes, Charsets.UTF_8)
			}
			byteRange.reversed().forEach {
				result = result.replaceRange(it.first, it.second)
			}

			return escape(result)
		}
	}

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return Companion.parse(event.msg)
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
}