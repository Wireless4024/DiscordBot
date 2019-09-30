package com.wireless4024.discordbot.internal

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import java.io.File

interface Property {
	companion object {
		/* @formatter:off */
		@JvmStatic
		val TOKEN:String
		@JvmStatic
		val YTTOKEN:String
		@JvmStatic
		val LOGGER_NAME = "DiscordBot"
		@JvmStatic
		var DEBUG:Boolean
		@JvmStatic
		val ADMIN_LIST:List<Long>
		@JvmStatic
		val COMMAND_TIMEOUT:Int
		@JvmStatic
		val LONG_TIMEOUT:Long
		@JvmStatic
		val LONG_TIMEOUT_MILLI:Long
		@JvmStatic
		val PREFIX:String
		val Commands: CommandPool by lazyOf(CommandPool())
		val ApplicationScope= CoroutineScope(Dispatchers.Default)
		val dbname="w4024-discordbot-v2"
		lateinit var JDA:JDA
		/* @formatter:on */
		init {
			val configFile = File("discordbot-config.json")
			if (!configFile.exists()) {
				configFile.writeText(ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(Config()))
				Utils.error("configuration file not found! please edit your config file!\nfile should be here ${configFile.absoluteFile}")
			}
			val cfg = ObjectMapper().readValue(configFile.readText(), Config::class.java)
			if (cfg.token in arrayOf("", "your discord bot token here")) {
				Utils.error("missing token! please edit your config file\nfile should be here ${configFile.absoluteFile}")
			}
			TOKEN = cfg.token
			YTTOKEN = cfg.yttoken
			DEBUG = cfg.debug
			ADMIN_LIST = cfg.adminlist
			COMMAND_TIMEOUT = cfg.executionTimeout
			LONG_TIMEOUT = cfg.messageDeleteDelay
			LONG_TIMEOUT_MILLI = LONG_TIMEOUT * 1000L
			PREFIX = cfg.prefix
		}

		data class Config(
			val token: String = "your discord bot token here",
			val yttoken: String = "",
			val debug: Boolean = false,
			val adminlist: List<Long> = emptyList(),
			val executionTimeout: Int = 5,
			val messageDeleteDelay: Long = 30,
			val prefix: String = "--"
		)

		interface Permission {
			companion object {
				val ANY = 0
				val MEMBER = 1 shl 2
				val ADMINISTRATOR = 1 shl 3
				val OWNER = 1 shl 4
				val DEV = 1 shl 5

				fun check(user: Member?, permission: Int = 0): Boolean {
					return when {
						user == null              -> false
						user.idLong in ADMIN_LIST -> true
						else                      -> when (permission) {
							permission and ANY           -> true
							permission and MEMBER        ->
								user.isOwner || user.hasPermission(net.dv8tion.jda.api.Permission.MESSAGE_WRITE)
							permission and ADMINISTRATOR ->
								user.isOwner || user.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)
							permission and OWNER         -> user.isOwner
							permission and DEV           -> user.idLong in ADMIN_LIST
							else                         -> false
						}
					}
				}
			}
		}
	}
}