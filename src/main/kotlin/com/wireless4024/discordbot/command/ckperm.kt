package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.SkipArguments
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

@SkipArguments
class ckperm : ICommandBase {

	override val permission = Permission.ADMINISTRATOR
	override val options: List<Option> = listOf()
	override fun invoke(args: CommandLine, event: MessageEvent): String {
		require(event.msg.isNotEmpty()) { "missing argument" }
		return if (event.chperm(event.msg.toInt())) "true" else "false"
	}

	/*@formatter:off*/
	private fun String.toInt(): Int = try{Integer.parseInt(this)}catch(e:NumberFormatException){0.inv()}
	/*@formatter:off*/
}