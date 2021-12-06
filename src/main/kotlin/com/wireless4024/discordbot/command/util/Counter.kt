package com.wireless4024.discordbot.command.util

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property
import kotlinx.coroutines.launch
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class Counter : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		Property.ApplicationScope.launch {
			val msg = event.ev?.message?.reply("```\nCounter : 0```")?.complete(false)
			if (msg != null) {
				msg.addReaction("\uD83D\uDD3D").complete()
				msg.addReaction("\u274c").complete()
				msg.addReaction("\uD83D\uDD3C").complete()
			}
		}
		return ""
	}

	override val options: List<Option> = listOf()
	override val permission: Int = 0
}