package com.wireless4024.discordbot.command.string

import com.wireless4024.discordbot.internal.*
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit.SECONDS

class regex : ICommandBase {

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		if (args.args.size < 3)
			throw CommandError("use case regex <regex> <operation> <target> [replacement]")
		return regex(args[0]!!, args[1]!!, args[2]!!, args[3])
	}

	companion object {
		@JvmStatic
		fun regex(regex: String, operation: String, target: String, replacement: String? = null): Any {
			return Utils.execute(5, SECONDS, Callable {
				try {
					when (operation.toLowerCase()) {
						"find"          -> regex.toRegex().find(target)?.value ?: ""
						"in", "findall" -> regex.toRegex().findAll(target).map { it.value }.joinToString(", ", "[", "]")
						"~", "match"    -> regex.toRegex().containsMatchIn(target)
						"~=", "matchs"  -> regex.toRegex().matches(target)
						"=", "replace"  -> if (replacement == null) with(split(target)) {
							regex.toRegex().replace(this[0], this[1])
						} else regex.toRegex().replace(target, replacement)
						"replacefirst"  -> if (replacement == null) with(split(target)) {
							regex.toRegex().replaceFirst(this[0], this[1])
						} else regex.toRegex().replaceFirst(target, replacement)
						"split"         -> if (replacement == null) with(split(target)) {
							if (this[1].parseInt() == null) regex.toRegex().split(target)
							else regex.toRegex().split(this[0], this[1].parseInt() ?: 0)
						} else regex.toRegex().split(target, replacement.parseInt() ?: 0)
						else            -> "invalid operation"
					}
				} catch (e: Throwable) {
					e.cause?.message ?: e.message ?: e
				}
			}) ?: "execution timeout"

		}

		@JvmStatic
		private fun split(base: String): Array<String> {
			val breakPoint = base.lastIndexOf(' ')
			if (breakPoint == -1)
				return arrayOf(base, "")
			val end = Utils.word.findAll(base).last()
			return arrayOf(base.removeRange(end.range), trim(end.value))
		}

		@JvmStatic
		fun trim(word: String): String {
			if (word.startsWith('\'') && word.endsWith('\''))
				return word.drop(1).dropLast(1)
			return word.trim()
		}
	}
}