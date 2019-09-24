package com.wireless4024.discordbot.command.string

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.SkipArguments
import com.wireless4024.discordbot.internal.parseInt
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

@SkipArguments
class repeat : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		val lim = randstr.endWithNum.find(event.msg)
		val content = if (lim != null) event.msg.removeRange(lim.range).trim() else event.msg
		val count = lim?.value?.trim()?.parseInt() ?: 2
		return content.repeat(count)
	}

	override val options: List<Option> = listOf()
	override val permission: Int = 0
}