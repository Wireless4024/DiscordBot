package com.wireless4024.discordbot.internal

import com.wireless4024.discordbot.internal.Property.Companion.Permission
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import java.util.concurrent.TimeUnit.SECONDS

open class MessageEvent(private val e: MessageReceivedEvent?, val raw: Boolean = false) {
	/**
	 * get original [MessageReceivedEvent] object from JDA
	 * @see net.dv8tion.jda.api.hooks.ListenerAdapter.onMessageReceived
	 */
	val ev
		get() = this.e

	/**
	 * get [MessageChannel] object that message recieved
	 * @see MessageReceivedEvent.getChannel
	 */
	val ch
		get() = this.e!!.channel

	/**
	 * get User object who send this message
	 * @see MessageReceivedEvent.getAuthor
	 */
	val user
		get() = this.e!!.author

	/**
	 * get Member object who send this message
	 * @see MessageReceivedEvent.getMember
	 */
	val member
		get() = this.e!!.member!!

	/**
	 * get [TextChannel] object that message recieveds
	 * @see MessageReceivedEvent.getTextChannel
	 */
	val txtch
		get() = this.e!!.textChannel

	/**
	 * reply Text or Message or MessageEmbed to TextChannel
	 * @see MessageChannel.sendMessage
	 */
	var reply: Any? = null
		set(text) = this.reply(text, deep = 1)
	var permreply: Any? = null
		set(text) = this.reply(text, true)
	open var msg = if (raw) this.e!!.message.contentDisplay else
		Utils.getParameter(this.e!!.message.contentDisplay.trim('`', ' '))
	val guild
		get() = this.e!!.guild
	val configuration
		get() = ConfigurationCache.get(guild, this)
	val voiceChannel
		get() = member.voiceState?.channel
	val musicController = configuration.musicController

	fun asRaw(): MessageEvent = MessageEvent(e, true)

	fun consume() = Utils.consume(ev)

	open fun reply(text: Any?, permanent: Boolean = false, deep: Int = 0) {
		runBlocking {
			launch {
				Utils.log("-> [reply]\t'$text'", deep = 11 + deep)
				ch.send(text) {
					if (!permanent) {
						try {
							it.delete().queueAfter(Property.LONG_TIMEOUT, SECONDS, {}, {})
						} catch (e: Exception) {
						}
					}
				}
			}
		}
	}

	fun dropFirst(): MessageEvent {
		this.msg = Utils.getParameter(this.msg)
		return this
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

	val noPermissionException = CommandError("you don't have permission to use command")
	fun ensurePermission(permission: Int) {
		if (!chperm(permission)) throw  noPermissionException
	}

	override fun toString(): String {
		return msg
	}
}

class ConsoleEvent(private val message: String) : MessageEvent(null) {
	override var msg: String = this.message

	override fun reply(text: Any?, permanent: Boolean, deep: Int) {
		if (text != null && text.toString().isNotBlank()) print(text)
	}

	override fun chperm(permission: Int): Boolean = true
}