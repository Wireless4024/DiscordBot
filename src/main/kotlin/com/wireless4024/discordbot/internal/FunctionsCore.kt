package com.wireless4024.discordbot.internal

import net.dv8tion.jda.api.entities.MessageChannel

fun MessageChannel.send(msg: Any?) {
	if (msg == null) return
	val message = msg.toString()
	if (message.isNotEmpty())
		this.sendMessage(message as CharSequence).queue()
}