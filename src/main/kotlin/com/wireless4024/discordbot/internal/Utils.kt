package com.wireless4024.discordbot.internal

import com.sedmelluq.lava.common.tools.DaemonThreadFactory
import com.wireless4024.discordbot.command.string.regex
import okhttp3.OkHttpClient
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.*
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern

class Utils {
	companion object {
		@JvmStatic
		private val ThreadFactory: ThreadFactory = DaemonThreadFactory("bot")
		@JvmStatic
		val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), ThreadFactory)
		@JvmStatic
		val scheduleexecutor = Executors.newScheduledThreadPool(1, ThreadFactory)
		@JvmStatic
		val logger: Logger = Logger.getLogger(Property.LOGGER_NAME)
		@JvmStatic
		val HTTPClient = OkHttpClient()
		@JvmStatic
		val URL_Regex = Pattern.compile(
			"^(http://www\\.|https://www\\.|http://|https://)?[a-z0-9]+([\\-.]{1}[a-z0-9]+)*\\.[a-z]{2,5}(:[0-9]{1,5})?(/.*)?\$"
		)

		@JvmStatic
		val globalEvent = Handler()

		@JvmStatic
		val HAS_HTTP = Pattern.compile("^http")

		@JvmStatic
		fun <T> execute(timeout: Long, unit: TimeUnit, callback: Callable<T?>): T? {
			val ex = Executors.newSingleThreadExecutor()
			return try {
				ex.submit(callback).get(timeout, unit)
			} catch (e: TimeoutException) {
				null
			} catch (e: Exception) {
				throw java.lang.RuntimeException(e)
			} finally {
				ex.shutdown()
			}
		}

		@JvmStatic
		fun log(msg: Any?, level: Level = Level.INFO, deep: Int = 0) {
			logger.logp(
				level,
				Thread.currentThread().stackTrace[3 + deep].className,
				Thread.currentThread().stackTrace[3 + deep].methodName,
				"@${Date()}\n" + msg.toString()
			)
		}

		@JvmStatic
		val regexisregex =
			Regex("^/((?![*+?])(?:[^\\r\\n\\[/\\\\]|\\\\.|\\[(?:[^\\r\\n\\]\\\\]|\\\\.)*])+)/")

		@JvmStatic
		fun ifRegex(string: String): String? {
			val inputRegex = regexisregex.find(string) ?: return null
			val operator = string.removeRange(inputRegex.range).trimStart().substringBefore(' ').trim()
			if (operator == "") return null
			val target = string.substringAfter(operator).trim()
			if (target == "") return null
			return regex.regex(inputRegex.value.trim('/'), operator, target).toString()
		}

		@JvmStatic
		fun getCommand(args: String, prefix: String): String {
			return if (args.indexOf(' ') == -1)
				args.substring(prefix.length)
			else
				args.substringBefore(' ').substring(prefix.length)
		}

		@JvmStatic
		fun getParameter(args: String): String {
			return if (args.indexOf(' ') == -1) "" else args.substringAfter(' ')
		}

		@JvmStatic
		fun urlExisted(url: String): Boolean {
			if (!URL_Regex.matcher(url).find())
				return false
			val furl = getFinalURL(if (HAS_HTTP.matcher(url).find()) url else "http://$url")
			println("checking url... '${furl}'")
			return ((URL(furl).openConnection() as HttpURLConnection).responseCode == 200)
		}

		@JvmStatic
		fun getFinalURL(url: String, deep: Int = 0): String {
			if (deep > 5)
				throw RuntimeException("redirection loop")
			val con = URL(url).openConnection() as HttpURLConnection
			con.instanceFollowRedirects = false
			con.connect()
			if (con.responseCode == HttpURLConnection.HTTP_MOVED_PERM || con.responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
				val redirectUrl = con.getHeaderField("Location")
				return getFinalURL(redirectUrl, deep + 1)
			}
			return url
		}

		@JvmStatic
		fun toReadableFormatTime(millis: Long): String {
			if (millis == 0L)
				return "0 sec"
			if (millis == Long.MAX_VALUE || millis < 0L)
				return "forever"

			val milli = millis % 1000
			val sec = (millis / 1000) % 60
			val min = ((millis / 1000) / 60) % 60
			val hour = (((millis / 1000) / 60 / 60)) % 60
			val day = ((((millis / 1000) / 60 / 60)) / 60) % 24
			var message = ""
			if (day > 0) {
				message += "$day day"
				if (day > 2) message += "s"
			}
			if (hour > 0) {
				message += " $hour hour"
				if (hour > 2) message += "s"
			}
			if (min > 0) {
				message += " $min minute"
				if (min > 2) message += "s"
			}
			if (sec > 0) {
				message += " $sec second"
				if (sec > 2) message += "s"
			}
			if (milli > 0) {
				message += " $sec milli"
				if (milli > 2) message += "s"
			}
			return message.trimStart()
		}
	}
}