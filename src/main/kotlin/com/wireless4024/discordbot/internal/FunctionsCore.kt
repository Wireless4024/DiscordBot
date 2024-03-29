package com.wireless4024.discordbot.internal

import ch.obermuhlner.math.big.BigDecimalMath
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageEmbed
import org.apache.commons.cli.CommandLine
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.TimeUnit

 fun MessageChannel.send(msg: Any?, success: (Message) -> Unit? = {}) {
	 if (msg == null) return
	 if (msg is MessageEmbed) {
		 this.sendMessageEmbeds(msg).queue { success(it) }
		 return
	 }
	 if (msg is EmbedBuilder) {
		 this.sendMessageEmbeds(msg.build()).queue { success(it) }
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
	timeout: Long = Property.LONG_TIMEOUT,
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

fun String.parseBigDecimal(): BigDecimal? {
	try {
		if (isEmpty()) return BigDecimal.ZERO
		return BigDecimalMath.toBigDecimal(this)
	} catch (e: Throwable) {
		return null
	}
}

fun String.hasBefore(char: Char, pos: Int): Boolean {
	var now = pos - 1
	var currentChar: Char
	while (now > 0) {
		currentChar = this[now]
		if (currentChar.isWhitespace())
			--now
		else return currentChar == char
	}
	return false
}