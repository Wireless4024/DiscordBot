package com.wireless4024.discordbot.internal

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.Options
import org.reflections.Reflections
import java.lang.reflect.Method

class CommandPool(path: String = "com.wireless4024.discordbot.command") {
	private var commands: MutableMap<String, Invokable> = mutableMapOf()

	init {
		val rfc = Reflections(path)
		rfc.getSubTypesOf(ICommandBase::class.java)
				.forEach { c ->
					run {
						val inst = c.getDeclaredConstructor()
								.newInstance()
						commands[inst.name().toLowerCase()] = InvokableInstance(inst)
						c.declaredMethods.filter { method ->
							(method.parameterTypes?.contentEquals(
									arrayOf(
											CommandLine::class.java,
											MessageEvent::class.java
									)
							) ?: false) && method.isAnnotationPresent(
									Command::class.java
							)
						}.forEach { method ->
							method.isAccessible = true
							if (!commands.containsKey(method.name.toLowerCase()))
								commands[method.name.toLowerCase()] = InvokableMethod(
										method, inst
								)
						}
					}
				}
	}

	fun get(): MutableCollection<Invokable> = commands.values
	operator fun get(string: String): Invokable? = commands[string.toLowerCase()]
	override fun toString(): String = commands.toString()
}

interface Invokable {
	val permission: Int
	fun name(): String
	fun parse(msg: Array<String>): CommandLine
	fun genOptions(): Options
	operator fun invoke(args: CommandLine, event: MessageEvent): Any
}

internal class InvokableInstance(val cm: ICommandBase) : Invokable {
	init {
		println("registered command ${cm.name()}")
	}

	override val permission: Int = cm.permission
	override fun name(): String = cm.name()
	override fun parse(msg: Array<String>): CommandLine = cm.parse(msg)
	override fun genOptions(): Options = cm.genOptions()
	override fun invoke(args: CommandLine, event: MessageEvent): Any = try {
		cm(args, event)
	} catch (e: Exception) {
		e.printStackTrace()
		e.cause?.message ?: e.message ?: ""
	}
}

internal class InvokableMethod(val method: Method, val parent: ICommandBase) : Invokable {
	init {
		println("registered command ${parent.name()}.${method.name}")
	}

	override fun genOptions(): Options = Options()
	override val permission: Int = method.getAnnotation(Command::class.java).permission
	override fun name(): String = method.name
	override fun parse(msg: Array<String>): CommandLine = parent.parse(msg)
	override fun invoke(args: CommandLine, event: MessageEvent): Any = try {
		method.invoke(parent, args, event)
	} catch (e: java.lang.reflect.InvocationTargetException) {
		e.printStackTrace()
		e.cause?.message ?: e.message ?: ""
	}
}