package com.wireless4024.discordbot.command.numeric

import com.keelar.exprk.Expressions
import com.wireless4024.discordbot.internal.CommandError
import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.get
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.math.RoundingMode.CEILING
import java.math.RoundingMode.FLOOR

class expression : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return when (args[0] ?: throw CommandError("Missing argument")) {
			"enter" -> {
				event.configuration.registerContext("expression",
					{ it.configuration.Expressions.evalRound(it.asRaw().msg) })
				"now you can type Arithmetic Expression into chat to execute calculate it!"
			}
			"rounding"  -> {
				when (args[1] ?: "now") {
					"floor" -> "changed rounding mode to ${event.configuration.Expressions.setRoundingMode(FLOOR).roundingMode.name}"
					"ceil"  -> "changed rounding mode to ${event.configuration.Expressions.setRoundingMode(CEILING).roundingMode.name}"
					"now"   -> event.configuration.Expressions.roundingMode.name
					else    -> "[floor, ceil]"
				}
			}
			"prec",
			"precision" -> {
				event.configuration.Expressions.setPrecision(args[1]?.toIntOrNull() ?: -1)
				"precision is ${event.configuration.Expressions.precision}"
			}
			"reset"     -> {
				event.configuration.Expressions = Expressions()
				"reset expression to default"
			}
			else        -> "unknown argument"
		}
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ADMINISTRATOR
}