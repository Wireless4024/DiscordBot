package com.wireless4024.discordbot.internal

import java.io.OutputStream

class FastFunction {
	companion object {
		fun startWith(string: String, char: Char): Boolean {
			return string.isNotEmpty() && string[0] == char
		}

	}
}

class CollectibleOutputStream : OutputStream() {
	private var str: StringBuffer = StringBuffer()
	override fun write(b: Int) {
		str.append(b.toChar())
	}

	fun collect(): String = this.str.toString()
}
