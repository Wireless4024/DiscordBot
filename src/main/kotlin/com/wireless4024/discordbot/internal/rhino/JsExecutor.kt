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
		engine.inject("global", STD.global)
		engine.allow(String::class.java)
		engine.evalWithGlobalScope(
			parent.guild.id, """
var EMPTY_OBJECT = {}
function print(val){std.print(val)}
function println(val){std.println(val)}
function get(url){return std.request(url,'GET',EMPTY_OBJECT)}
function get(url,data){return std.request(url,'GET',data)}
function post(url){return std.request(url,'POST',EMPTY_OBJECT)}
function post(url,data){return std.request(url,'POST',data)}
function request(url){return std.request(url,'GET',EMPTY_OBJECT)}
function request(url,method){return std.request(url,method,EMPTY_OBJECT)}
function request(url,method,data){return std.request(url,method,data)}
function clearGlobal(){std.clearGlobal()}
"""
		)
	}

	fun eval(js: String): String {
		try {
			val obj = engine.eval(
				parent.guild.id, """${STD.globalString()};
$js"""
			)
			if (STD.stdout.isEmpty())
				if (obj is Undefined)
					STD.print("undefined")
				else
					STD.print(obj)
		} catch (e: Throwable) {
			val msg = e.cause?.toString() ?: e.toString()
			if (msg.contains("is not defined")) {
				val out = STD.stdout
				if (out.isEmpty())
					out.append("undefined")
			} else
				STD.print("\n" + msg)
		}
		return STD.collectSTDOUT()
	}
}