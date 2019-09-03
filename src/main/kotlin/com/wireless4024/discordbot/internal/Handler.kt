package com.wireless4024.discordbot.internal

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.ChannelType.TEXT
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit.SECONDS

class Handler : ListenerAdapter() {
	private fun Member.getFullName(): String {
		return if (this.nickname != null) "${this.nickname}(${this.user.name})" else this.user.name
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if ((!event.isFromType(TEXT)) || event.author.isBot || event.author.isFake)
			return
		runBlocking {
			launch {
				val message = event.message
				val messageText = message.contentDisplay.replace(Regex("\\s+"), " ")
				Utils.log("[${message.member!!.getFullName()}] : $messageText", deep = 2)
				if (FastFunction.startWith(messageText, '=')) {
					val number = Utils.execute(5,
					                           SECONDS,
					                           Callable { Property.Expressions.evalToString(messageText.substring(1)) })
					             ?: "Execution timeout"
					if (number == "Execution timeout")
						Utils.log("'${messageText}' execute too long")
					MessageEvent(event).reply(number)
				}
				if (messageText.startsWith(Property.PREFIX)) {
					ICommandBase.invokeCommand(
							Property.Commands[Utils.getCommand(messageText)],
							Utils.getParameter(messageText),
							MessageEvent(event)
					)
					GlobalScope.launch {
						delay(Property.BASE_SLEEP_DELAY_MILLI)
						event.message.delete().queue()
					}
				}
			}
		}
	}
}