package com.wireless4024.discordbot.internal

import com.wireless4024.discordbot.internal.Property.Companion.Permission
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.concurrent.TimeUnit.SECONDS

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
		set(text) = this.reply(text, deep = 1)
	var permreply: Any? = null
		set(text) = this.reply(text, true)
	open val msg = Utils.getParameter(this.e!!.message.contentDisplay.trim('`', ' '))
	val guild
		get() = this.e!!.guild
	val configuration
		get() = ConfigurationCache.get(guild, this)
	val voiceChannel
		get() = member.voiceState?.channel
	val musicController = configuration.musicController

	open fun reply(text: Any?, permanent: Boolean = false, deep: Int = 0) {
		runBlocking {
			launch {
				Utils.log("-> [reply]\t'$text'", deep = 11 + deep)
				ch.send(text) {
					if (!permanent) {
						try {
							it.delete().queueAfter(Property.BASE_SLEEP_DELAY, SECONDS)
						} catch (e: Exception) {
						}
					}
				}
			}
		}
	}

	open fun chperm(permission: Int): Boolean {
		return Permission.check(member, permission)
	}

	fun <T> ensureVoiceConnected(then: (() -> T)): T {
		if (member.voiceState?.channel == null)
			throw CommandError("You must be in voice channel to use command")
		return then.invoke()
	}

	fun <T> ensureVoiceConnected(then: T): T {
		if (member.voiceState?.channel == null)
			throw CommandError("You must be in voice channel to use command")
		return then
	}

	fun ensureVoiceConnected() {
		if (member.voiceState?.channel == null)
			throw CommandError("You must be in voice channel to use command")
	}

	override fun toString(): String {
		return msg
	}
}

class ConsoleEvent(private val message: String) : MessageEvent(null) {
	override val msg: String = this.message

	override fun reply(text: Any?, permanent: Boolean, deep: Int) {
		if (text != null && text.toString().isNotBlank()) print(text)
	}

	override fun chperm(permission: Int): Boolean = true
}