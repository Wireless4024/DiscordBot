package com.wireless4024.discordbot.command.string

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.SkipArguments
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.net.URLDecoder
import java.net.URLEncoder

@SkipArguments
class encodeurl : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return URLEncoder.encode(event.msg, Charsets.UTF_8)
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY

}

@SkipArguments
class decodeurl : ICommandBase {

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		return URLDecoder.decode(event.msg, Charsets.UTF_8)
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY

}