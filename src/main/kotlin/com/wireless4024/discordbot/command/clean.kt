package com.wireless4024.discordbot.command

import com.wireless4024.discordbot.internal.ICommandBase
import com.wireless4024.discordbot.internal.MessageEvent
import com.wireless4024.discordbot.internal.Property.Companion.Permission
import com.wireless4024.discordbot.internal.SkipArguments
import com.wireless4024.discordbot.internal.parseInt
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Option
import java.util.concurrent.atomic.AtomicLong

@SkipArguments
class clean : ICommandBase {

	override val permission = Permission.ADMINISTRATOR
	override val options: List<Option> = listOf()

	override fun invoke(args: CommandLine, event: MessageEvent): String {
		if (event.ev == null || event.configuration["clean"])
			return ""
		event.configuration["clean"] = true
		val limit = event.msg.parseInt() ?: 0
		var cnt = AtomicLong()

		val msg = event.ch.sendMessage("deleting message")

		if (limit == 0)
			event.ch.iterableHistory.forEach { m -> m.delete().queue() }.let {
				msg.content("deleted ${cnt.incrementAndGet()}")
				event.configuration["clean"] = false
			}
		else event.ch.iterableHistory.limit(limit + 1)
			.complete()
			.drop(1)
			.forEach { m ->
				try {
					m.delete().queue()
				} catch (e: Exception) {
				}
			}.let {
				msg.content("deleted ${cnt.incrementAndGet()}")
				event.configuration["clean"] = false
			}
		if (cnt.get() == 0L) {
			msg.content("No message deleted")
		}
		return ""
	}
}