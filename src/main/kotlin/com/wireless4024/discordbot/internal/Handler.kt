package com.wireless4024.discordbot.internal

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class Handler : ListenerAdapter() {

	private fun MessageReceivedEvent.reply(msg: String) {
		return this.channel.sendMessage(msg).queue()
	}

	private fun Member.getFullName(): String {
		return if (this.nickname != null) "${this.nickname}(${this.user.name})" else this.user.name
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (event.author.isBot || event.author.isFake)
			return

		val message = event.message
		val messageText = message.contentDisplay
		Utils.log("[${message.member!!.getFullName()}] : $messageText")
		if (FastFunction.startWith(messageText, '='))
			event.reply(Property.Expressions.evalToString(messageText.substring(1)))
		if (messageText.startsWith(Property.PREFIX))
			ICommandBase.invokeCommand(
					Property.Commands[Utils.getCommand(messageText)],
					Utils.getParameter(messageText),
					MessageEvent(event)
			)
	}
}