package com.wireless4024.discordbot.command.string

import com.wireless4024.discordbot.internal.CommandError
import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.get
import com.wireless4024.utils.JUtils
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class format : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		if (args.args.isEmpty()) throw CommandError("usage format <format> [args]")
		return JUtils.format(args[0] ?: "", args.args.let { it.sliceArray(1 until it.size) })
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
}