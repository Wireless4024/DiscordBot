package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.get
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.Security

class hash : ICommandBase {
	companion object {
		@JvmStatic
		val algorithms = Security.getProviders().map { it.services }
			.reduce { a, b -> a + b }
			.filter { it.type == MessageDigest::class.java.simpleName }.map { it.algorithm }
	}

	override fun invoke(args: CommandLine, event: MessageEvent): Any {
		if (args[0]?.startsWith("alg") == true)
			return algorithms
		if (args.args.size < 2)
			return "use case hash <algorithm> <text> or 'hash alg' to list algorithms"
		return try {
			Security.getProviders().asSequence().map { it.services }.reduce { a, b -> a + b }
			val instance = MessageDigest.getInstance(args[0])
			instance.digest(args[1]?.toByteArray() ?: ByteArray(0))
				.joinToString("") { String.format("%02x", it) }
		} catch (e: NoSuchAlgorithmException) {
			"algorithm not found"
		} catch (e: NullPointerException) {
			"algorithm is null?"
		}
	}

	override val options: List<Option> = listOf()
	override val permission: Int = Permission.ANY
}