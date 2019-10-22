package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.SkipArguments
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

@SkipArguments
class js : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		if (event.msg.equals("enter", true)) {
			event.configuration.registerContext(
				"js",
				event.ch.idLong,
				{ "```\n${it.configuration.JavascriptEngine.eval(it.msg)}\n```" })
			return "now you can type Javascript script into chat to execute Javascript!"
		}
		return "```${event.configuration.JavascriptEngine.eval(event.msg)}```"
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
}