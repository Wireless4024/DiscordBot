package com.wireless4024.discordbot.command.network

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.net.URL
import java.net.URLConnection

class header : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		if (event.msg.isEmpty())
			return "use case header <url>"
		val obj = URL(event.msg)
		val conn: URLConnection = obj.openConnection()
		val headers = StringBuilder()
		conn.headerFields.forEach { (key, value) -> headers.append(key).append("=").append(value[0]).append('\n') }
		return headers
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY

}