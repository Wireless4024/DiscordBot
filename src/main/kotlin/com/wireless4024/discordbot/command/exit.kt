package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.*
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

@SkipArguments
class exit : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Nothing {
		ConfigurationCache.submit()
		TimeUnit.SECONDS.sleep(3)
		event.ev!!.message.delete().complete()
		exitProcess(0)
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Property.Companion.Permission.DEV
}