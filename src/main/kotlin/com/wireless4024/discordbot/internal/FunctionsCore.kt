package com.wireless4024.discordbot.internal

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.exceptions.ContextException
import org.apache.commons.cli.CommandLine
import java.util.concurrent.TimeUnit

fun MessageChannel.send(msg: Any?, success: (Message) -> Unit? = {}) {
	if (msg == null) return
	if (msg is MessageEmbed) {
		this.sendMessage(msg).queue { success(it) }
		return
	}
	if (msg is EmbedBuilder) {
		this.sendMessage(msg.build()).queue { success(it) }
		return
	}
	if (msg is Message) {
		this.sendMessage(msg).queue { success(it) }
		return
	}
	if (msg is MessageBuilder) {
		this.sendMessage(msg.build()).queue { success(it) }
		return
	}
	val message = msg.toString().trim()
	if (message.isNotEmpty())
		this.sendMessage(message as CharSequence).queue { success(it) }
}

fun MessageChannel.sendThenDelete(
		msg: Any?,
		timeout: Long = Property.BASE_SLEEP_DELAY,
		timeUnit: TimeUnit = TimeUnit.SECONDS
) {
	this.send(msg) {
		if (timeout > 0)
		/*@formatter:off*/
			try {
				timeUnit.sleep(timeout).run { it.delete().queue() }
			} catch (e: ContextException) {
			}
		/*@formatter:on*/
	}
}

fun MessageEmbed.toString(): String {
	var str = ""
	this.fields.forEach { str += "${it.name} -> ${it.value}" }
	return """----EMBED----
${this.title}
${this.description}
by ${this.author}
$str
----EMBED----"""
}

operator fun CommandLine.get(index: Int): String? {
	return this.args[index]
}

operator fun CommandLine.get(option: String): String? {
	return this.getOptionValue(option)
}

fun CommandLine.dropFirst(): CommandLine {
	return CommandLine.Builder().also {
		this.args.drop(1).forEach { i -> it.addArg(i) }
		this.options.forEach { i -> it.addOption(i) }
	}.build()
}

fun Any.isUnit() = this::class == Unit::class