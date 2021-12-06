package com.wireless4024.discordbot.command.string

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.SkipArguments
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.util.*

@SkipArguments
class touppercase : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return event.msg.toUpperCase()
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
}

@SkipArguments
class tolowercase : ICommandBase {

    override fun invoke(args: CommandLine, event: MessageEvent): Any {
        return event.msg.lowercase(Locale.getDefault())
    }

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
}

@SkipArguments
class capitalize : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return event.msg.capitalize()
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
}

@SkipArguments
class torandomcase : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		val rnd = java.util.Random()
		return String(event.msg.toCharArray().map { if (rnd.nextBoolean()) it.toUpperCase() else it.toLowerCase() }.toCharArray())
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
}

@SkipArguments
class invertcase : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return String(event.msg.toCharArray().map { if (it.isLowerCase()) it.toUpperCase() else it.toLowerCase() }.toCharArray())
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
}