package com.wireless4024.discordbot.internal

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import org.apache.commons.cli.CommandLine
import java.math.BigInteger
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
				timeUnit.sleep(timeout).run { it.delete().queue({},{}) }
			} catch (e: Throwable) {
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
	return if (index < 0 || this.args.size <= index) null else this.args[index]
}

operator fun CommandLine.get(option: String): String? {
	return this.getOptionValue(option)
}

fun CommandLine.dropFirst(): CommandLine {
	return CommandLine.Builder().also {
		if (args.isNotEmpty())
			this.args.drop(1).forEach { i -> it.addArg(i) }
		this.options.forEach { i -> it.addOption(i) }
	}.build()
}

fun Any.isUnit() = this::class == Unit::class

fun String.parseInt(): Int? {
	if (isEmpty()) return 0
	if (this.startsWith("0b", true)) return this.drop(2).toIntOrNull(2)
	if (this.startsWith("0x", true)) return this.drop(2).toIntOrNull(16)
	if (this.startsWith("0", true)) return this.drop(1).toIntOrNull(8)
	return this.toIntOrNull()
}

fun String.parseBigInteger(): BigInteger? {
	if (isEmpty()) return BigInteger.ZERO
	if (this.startsWith("0b", true)) return this.drop(2).toBigIntegerOrNull(2)
	if (this.startsWith("0x", true)) return this.drop(2).toBigIntegerOrNull(16)
	if (this.startsWith("0", true)) return this.drop(1).toBigIntegerOrNull(8)
	return this.toBigIntegerOrNull()
}