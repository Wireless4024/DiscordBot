package com.wireless4024.discordbot.internal

import org.reflections.Reflections

class CommandPool(
		path: String = "com.wireless4024.discordbot.command",
		private var commands: MutableMap<String, ICommandBase> = mutableMapOf()
) {

	init {
		val rfc = Reflections(path)
		rfc.getSubTypesOf(ICommandBase::class.java)
				.forEach { c ->
					run {
						val inst = c.getDeclaredConstructor()
								.newInstance();commands[inst.name()] = inst
					}
				}
	}

	fun get(): MutableCollection<ICommandBase> {
		return commands.values
	}

	operator fun get(string: String): ICommandBase? {
		return this.commands[string]
	}

	override fun toString(): String = commands.toString()
}