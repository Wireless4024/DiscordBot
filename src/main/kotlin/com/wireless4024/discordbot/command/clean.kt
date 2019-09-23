package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.sendThenDelete
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

class clean : ICommandBase {
	private val running = mutableListOf<Long>()
	override val permission = Permission.ADMINISTRATOR
	override val options: List<Option> =
		listOf(
			Option("l", "limit", true, "number of message to remove").also { it.isRequired = false }
		)

	override fun invoke(args: CommandLine, event: MessageEvent): String {
		if (event.ev == null || event.ch.idLong in running)
			return ""
		running.add(event.ch.idLong)
		var cnt = 0
		if (!args.hasOption("l"))
			event.ch.iterableHistory.forEach { m -> m.delete().complete().also { ++cnt } }.let {
				event.ch.sendThenDelete("deleted $cnt messages")
				running.remove(event.ch.idLong)
			}
		else event.ch.iterableHistory.limit((args.getOptionValue("l").toIntOrNull() ?: 1) + 1)
			.complete()
			.drop(1)
			.forEach { m ->
				try {
					m.delete().complete().also { ++cnt }
				} catch (e: Exception) {
				}
			}.let {
				event.ch.sendThenDelete("deleted $cnt messages")
				running.remove(event.ch.idLong)
			}
		return ""
	}
}