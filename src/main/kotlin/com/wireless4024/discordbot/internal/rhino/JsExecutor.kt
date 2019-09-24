package com.wireless4024.discordbot.internal.rhino

import com.wireless4024.discordbot.internal.ConfigurationCache
import com.wireless4024.discordbot.internal.Property
import delight.rhinosandox.RhinoSandboxes
import org.mozilla.javascript.Undefined

class JsExecutor(val parent: ConfigurationCache) {
	private val engine = RhinoSandboxes.create()
	private val STD = StandardLib()

	init {
		engine.setMaxDuration(Property.COMMAND_TIMEOUT * 1000)
		engine.setInstructionLimit(50000)
		engine.inject("std", STD)
		engine.evalWithGlobalScope(
			parent.guild.id, """
function print(val){std.print(val)}
function println(val){std.println(val)}
"""
		)
	}

	fun eval(js: String): String {
		try {
			val obj = engine.eval(parent.guild.id, js)
			if (STD.stdout.isEmpty())
				if (obj is Undefined)
					STD.print("undefined")
				else
					STD.print(obj)
		} catch (e: Throwable) {
			STD.print("\n" + (e.cause?.toString() ?: e.toString()))
		}
		return STD.collectSTDOUT()
	}
}