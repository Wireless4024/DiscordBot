package com.wireless4024.discordbot.command.numeric

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.SkipArguments
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

@SkipArguments
class fx : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any = event.configuration.FunctionX(event.msg)

	override val options: List<Option> = emptyList()
	override val permission: Int = Permission.ANY

}