package com.wireless4024.discordbot.internal

import com.wireless4024.discordbot.internal.Property.Companion.ApplicationScope
import com.wireless4024.discordbot.internal.Utils.Companion.consume
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.ChannelType.TEXT
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.TimeUnit.SECONDS

class Handler : ListenerAdapter() {
	companion object {

		@JvmField
		val instance = Handler()
	}

	private fun Member.getFullName(): String {
		val nick = nickname // avoid method call
		val name = user.name
		return if (nick != null) "${nick}(${name})" else name
	}

	private inline val Message.senderName get() = member!!.getFullName()

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if ((!event.isFromType(TEXT)) || event.author.isBot || event.author.isFake)
			return

		ApplicationScope.launch {
			val message = event.message
			val messageText = message.contentDisplay
			val ev = MessageEvent(event)
			Utils.log("[${message.senderName}] : $messageText", deep = 2)

			if (ev.configuration.runContext(ev)) return@launch

			val re = if (message.contentRaw.startsWith('/')) Utils.ifRegex(message.contentRaw) else null
			if (re != null) {
				ev.reply(re)
				consume(event)
				return@launch
			}
			if (FastFunction.startWith(messageText, '=')) {
				val number = Utils.execute(
					5,
					SECONDS
				) {
					ev.configuration.Expressions
						.evalToString(messageText.substring(1))
				} ?: "Execution timeout"
				if (number == "Execution timeout")
					Utils.log("'${messageText}' execute too long")
				ev.reply(number)
				consume(event)
			}
			if (messageText.startsWith(ev.configuration.prefix)) {
				ICommandBase.invokeCommand(
					Property.Commands[Utils.getCommand(messageText, ev.configuration.prefix)],
					Utils.getParameter(messageText),
					ev
				)
				consume(event)
			}
		}
	}
}