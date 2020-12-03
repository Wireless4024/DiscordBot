package com.wireless4024.discordbot.internal

class Context(val name: String,
              val action: (MessageEvent) -> Any?,
              val whenExit: (MessageEvent) -> Unit) {

	private var tries: Int = 0
	suspend operator fun invoke(evt: MessageEvent): Boolean {
		val ev = evt.asRaw()
		if (tries > 4) {
			ev.reply = "context failed $tries times exiting"
			whenExit(ev)
			return true
		}
		if (ev.msg.equals("exit", true)) {
			ev.reply = "closed $name context"
			whenExit(ev)
			ev.consume()
			return true
		}
		return try {
			ev.reply(action(ev))
			true
		} catch (e: Throwable) {
			if (Property.DEBUG) e.printStackTrace()
			ev.reply(e.message ?: e.toString())
			++tries
			false
		} finally {
			ev.consume()
		}
	}
}