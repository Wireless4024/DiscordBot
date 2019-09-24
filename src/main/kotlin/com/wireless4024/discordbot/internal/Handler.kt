package com.wireless4024.discordbot.internal

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.ChannelType.TEXT
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit.SECONDS
import kotlinx.coroutines.launch as launch1

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
				val messageText = message.contentDisplay
				val ev = MessageEvent(event)
				Utils.log("[${message.member!!.getFullName()}] : $messageText", deep = 2)
				val re = if (message.contentRaw.startsWith('/')) Utils.ifRegex(message.contentRaw) else null
				if (re != null) {
					GlobalScope.launch1 {
						MessageEvent(event).reply(re)
						try {
							if (event.responseNumber != -1L)
								event.message.delete().completeAfter(Property.BASE_SLEEP_DELAY, SECONDS)
						} catch (e: Exception) {
						}
					}
					return@launch
				}
				if (FastFunction.startWith(messageText, '=')) {
					val number = Utils.execute(5,
					                           SECONDS,
					                           Callable {
						                           ev.configuration.Expressions
							                           .evalToString(messageText.substring(1))
					                           })
						?: "Execution timeout"
					if (number == "Execution timeout")
						Utils.log("'${messageText}' execute too long")
					MessageEvent(event).reply(number)
					GlobalScope.launch1 {
						try {
							if (event.responseNumber != -1L)
								event.message.delete().completeAfter(Property.BASE_SLEEP_DELAY, SECONDS)
						} catch (e: Exception) {
						}
					}
				}
				if (messageText.startsWith(ev.configuration.prefix)) {
					ICommandBase.invokeCommand(
						Property.Commands[Utils.getCommand(messageText, ev.configuration.prefix)],
						Utils.getParameter(messageText),
						ev
					)
					GlobalScope.launch1 {
						try {
							if (event.responseNumber != -1L)
								event.message.delete().completeAfter(Property.BASE_SLEEP_DELAY, SECONDS)
						} catch (e: Exception) {
						}
					}
				}
			}
		}
	}
}