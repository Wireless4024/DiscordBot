package com.wireless4024.discordbot.internal.rhino

import com.wireless4024.discordbot.internal.ConfigurationCache
import com.wireless4024.discordbot.internal.Property
import delight.rhinosandox.RhinoSandboxes

class JsExecutor(val parent: ConfigurationCache) {
	private val engine = RhinoSandboxes.create()
	private val STD = StandardLib()

	init {
		engine.setMaxDuration(Property.COMMAND_TIMEOUT * 1000)
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
			engine.eval(parent.guild.id, js)
		} catch (e: Throwable) {
			STD.print("\n" + (e.cause?.message ?: e.message ?: ""))
		}
		return STD.collectSTDOUT()
	}
}