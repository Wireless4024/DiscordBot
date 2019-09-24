package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.*
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option

@SkipArguments
class clean : ICommandBase {

	private val running = mutableListOf<Long>()
	override val permission = Permission.ADMINISTRATOR
	override val options: List<Option> = listOf()

	override fun invoke(args: CommandLine, event: MessageEvent): String {
		if (event.ev == null || event.ch.idLong in running)
			return ""
		running.add(event.ch.idLong)
		val limit = event.msg.parseInt() ?: 0
		var cnt = 0
		if (limit == 0)
			event.ch.iterableHistory.forEach { m -> m.delete().complete().also { ++cnt } }.let {
				event.ch.sendThenDelete("deleted $cnt messages")
				running.remove(event.ch.idLong)
			}
		else event.ch.iterableHistory.limit(limit + 1)
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