package com.wireless4024.discordbot.internal

import com.wireless4024.discordbot.internal.Property.Companion.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

open class MessageEvent(private val e: MessageReceivedEvent?) {
	val ev
		get() = this.e
	val ch
		get() = this.e!!.channel
	val user
		get() = this.e!!.author
	val member
		get() = this.e!!.member!!
	val txtch
		get() = this.e!!.textChannel
	var reply: Any? = null
		set(text) = this.reply(text)

	open fun reply(text: Any?) {
		this.ch.send(text)
	}

	open fun chperm(permission: Int): Boolean {
		return Permission.check(member, permission)
	}
}

class ConsoleEvent : MessageEvent(null) {
	override fun reply(text: Any?) {
		if (text != null && text.toString().isNotBlank()) print(text)
	}

	override fun chperm(permission: Int): Boolean {
		return true
	}
}