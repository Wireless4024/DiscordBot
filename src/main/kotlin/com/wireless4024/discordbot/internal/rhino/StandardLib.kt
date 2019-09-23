package com.wireless4024.discordbot.internal.rhino

class StandardLib {
	val stdout = StringBuilder()

	fun print(value: Any?) {
		stdout.append(value)
	}

	fun println(value: Any?) {
		stdout.append(value).append('\n')
	}

	fun collectSTDOUT(): String {
		val str = stdout.toString()
		stdout.clear()
		return str.trim().let { if (it.isEmpty()) "nothing return from your function" else it }
	}
}