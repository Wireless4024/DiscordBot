package com.wireless4024.discordbot.command.network

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.SkipArguments
import com.wireless4024.discordbot.internal.get
import com.wireless4024.discordbot.internal.rhino.StandardLib
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

@SkipArguments
class get : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return StandardLib.INSTANCE.request(event.msg, "GET")
	}

	override val options: List<Option> = listOf()
	override val permission: Int = 0
}

@SkipArguments
class head : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return StandardLib.INSTANCE.request(event.msg, "HEAD")
	}

	override val options: List<Option> = listOf()
	override val permission: Int = 0
}

class post : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		if (args.args.isEmpty())
			return "use case post <url> [parameter=value]"
		return StandardLib.INSTANCE.request(args[0] ?: "", "POST", args[1] ?: "")
	}

	override val options: List<Option> = listOf()
	override val permission: Int = 0
}

class request : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		if (args.args.size < 2)
			return "use case request <url> <method> [json]"
		return StandardLib.INSTANCE.request(args[0] ?: "", args[1] ?: "GET", args[2] ?: "")
	}

	override val options: List<Option> = listOf()
	override val permission: Int = 0
}