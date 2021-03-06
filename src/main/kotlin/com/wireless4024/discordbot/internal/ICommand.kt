package com.wireless4024.discordbot.internal

import com.wireless4024.discordbot.internal.Property.Companion.Permission
import org.apache.commons.cli.*
import java.io.PrintWriter

interface ICommandBase {
	companion object {
		@JvmStatic
		val EmptyCommandLine = DefaultParser().parse(Options(), emptyArray())!!

		@JvmStatic
		fun invokeCommand(cm: Invokable?, args: String, event: MessageEvent) {
			if (cm == null) {
				if (event.ev == null)
					println("invalid command")
				else
					event.reply = "invalid command"
				return
			}
			try {
				if (!Permission.check(event.member, cm.permission)) {
					Utils.log("failed to invoke command '${cm.name()}' user don't have permission")
					return
				}
				Utils.log("Invoking command '${cm.name()}'")
				val msg = cm(
					if (cm.needArguments()) cm.parse(Utils.split(args).toTypedArray()) else EmptyCommandLine,
					event
				)
				if (msg?.isUnit() != true)
					event.reply = msg
				else
					Utils.log("command '${cm.name()}' return nothing..")
			} catch (e: ParseException) {
				if (Property.DEBUG)
					e.printStackTrace()
				event.reply = run {
					val opt = CollectibleOutputStream()
					HelpFormatter().printHelp(
							PrintWriter(opt), 30,
							cm.name(), "",
							cm.genOptions(),
							0, 0, ""
					)
					opt.collect()
				}
			} catch (e: Throwable) {
				if (Property.DEBUG)
					e.printStackTrace()
				event.reply = e
			}
		}
	}

	@Throws
	operator fun invoke(args: CommandLine = EmptyCommandLine, event: MessageEvent): Any

	fun name(): String = javaClass.simpleName

	val options: List<Option>

	val permission: Int

	fun genOptions() = Options().also { self -> this.options.forEach { o -> self.addOption(o) } }

	fun parse(str: Array<String>): CommandLine {
		return DefaultParser().parse(genOptions(), str)
	}
}

annotation class Command(val permission: Int = 0)