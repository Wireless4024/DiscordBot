package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import kotlin.system.exitProcess

class exit : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Nothing {
		event.ev!!.message.delete().complete()
		exitProcess(0)
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Property.Companion.Permission.DEV
}