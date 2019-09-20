package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.Utils
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class resend : ICommandBase {
	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		val message = event.ch
			.getHistoryBefore(event.ev!!.message, 40)
			.complete()
			.retrievedHistory
			.first {
				it.author.idLong == event.member.idLong && !it.contentRaw.let {
					it.isNotEmpty() && it.contains(
						"resend",
						true
					)
				}
			}
		Utils.globalEvent.onMessageReceived(MessageReceivedEvent(event.ev!!.jda, -1, message))
		return "resend '${message.contentRaw}'"
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY

}