package com.wireless4024.discordbot.internal

import com.sedmelluq.lava.common.tools.DaemonThreadFactory
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class Utils {
	companion object {
		init {

		}

		private val ThreadFactory: ThreadFactory = DaemonThreadFactory("bot")
		val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), ThreadFactory)
		val scheduleexecutor = Executors.newScheduledThreadPool(1, ThreadFactory)
		val logger = Logger.getLogger(Property.LOGGER_NAME)

		fun execute(task: Runnable) {
			execute(Callable { task.run() })
		}

		fun <T> execute(task: Callable<T?>, timeout: Long = 3, unit: TimeUnit = TimeUnit.SECONDS): T? {
			val future = executor.submit(task)
			try {
				return future.get(timeout, unit)
			} catch (e: Exception) {
				log("Failed to execute task $task cause $e", Level.WARNING)
			} finally {
				future.cancel(true)
			}
			return null
		}

		fun log(msg: Any?, level: Level = Level.INFO) {
			logger.log(level, "@${Date()}\n" + msg.toString())
		}

		fun getCommand(args: String): String {
			return if (args.indexOf(' ') == -1)
				args.substring(Property.PREFIX.length)
			else
				args.substringBefore(' ').substring(Property.PREFIX.length)
		}

		fun getParameter(args: String): String {
			return if (args.indexOf(' ') == -1) "" else args.substringAfter(' ')
		}
	}
}