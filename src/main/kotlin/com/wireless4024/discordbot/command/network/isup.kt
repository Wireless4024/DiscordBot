package com.wireless4024.discordbot.command.network

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.get
import com.wireless4024.discordbot.internal.parseInt
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.net.InetAddress

class isup : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		if (event.msg.isEmpty())
			return "use case isup <ip>"

		val host = InetAddress.getByName(args[0]!!)
		val reachable = host.isReachable(args.getOptionValue("t", "30").parseInt() ?: 30)
		return "host ${args[0]} (${host.hostAddress}) is ${if (reachable) "up" else "down"}"
	}

	override val options: List<Option> = listOf(
		Option("t", "timeout", true, "timeout in second").also { it.isRequired = false }
	)
	override val permission: Int = Permission.ANY
}