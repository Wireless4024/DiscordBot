package com.wireless4024.discordbot.internal

import com.wireless4024.discordbot.internal.Property.Companion.ApplicationScope
import com.wireless4024.discordbot.internal.Utils.Companion.consume
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.ChannelType.TEXT
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.util.*
import java.util.concurrent.TimeUnit.MINUTES
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

	override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
		if ((!event.isFromType(TEXT)) || event.user?.isBot != false || event.user?.isSystem != false)
			return
		val reaction = event.reaction
		val emote = reaction.reactionEmote
		if (!emote.isEmoji) return
		val action: Int = when (emote.asCodepoints) {
			"U+1f53c" -> 2 // up
			"U+274c" -> 1// close
			"U+1f53d" -> 0 // down
			else -> -1
		}
		if (action != -1) {
			event.retrieveMessage().queue {
				if (action == 1) {
					it.delete().queue()
				} else
					if (it.contentRaw.startsWith("```\nCounter"))
						it.editMessage(it.contentRaw.replace(Regex("-?\\d+")) {
							var value = it.value.toLongOrNull() ?: 0
							if (action == 2) ++value else --value
							value.toString()
						}).queue()
				reaction.removeReaction(event.user!!).queue()
			}
		}
	}

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if ((!event.isFromType(TEXT)) || event.author.isBot || event.author.isSystem)
			return

		ApplicationScope.launch {
			val message = event.message
			val messageText = message.contentDisplay
			val ev = MessageEvent(event)
			Utils.log("[${message.senderName}] : $messageText", deep = 2)

			if (ev.configuration.runContext(ev)) return@launch

			if (ev.txtch.idLong == 891394873163391016L) {
				val wait = messageText.toLongOrNull() ?: 0
				val msg = "Wake up trainer we have legendary to catch ${
					event.guild.getEmotesByName("DoggoAngry", true).first().asMention
				}${event.guild.getEmotesByName("pika", true).first().asMention}"

				if (wait == 0L) {
					ev.reply = msg
					return@launch
				}
				Utils.scheduleexecutor.schedule({
					ev.reply = msg
				}, wait, MINUTES)
				event.textChannel.sendMessage(
					"schedule to notify in next $wait minutes (${
						Date(
							System.currentTimeMillis() + MINUTES.toMillis(wait)
						)
					})"
				).queue {
					it.delete().queueAfter(wait - 1, MINUTES)
				}
				message.delete().queue()
				return@launch
			}

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