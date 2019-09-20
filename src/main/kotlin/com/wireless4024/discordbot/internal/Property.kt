package com.wireless4024.discordbot.internal

import net.dv8tion.jda.api.entities.Member

interface Property {
	companion object {
		/* @formatter:off */
		@JvmStatic
		val TOKEN = ""
		@JvmStatic
		val YTTOKEN = ""
		@JvmStatic
		val LOGGER_NAME = "DiscordBot"
		@JvmStatic
		val DEV_LIST = listOf(298273616704045057L)
		@JvmStatic
		val BASE_SLEEP_DELAY: Long = 30
		@JvmStatic
		val BASE_SLEEP_DELAY_MILLI = BASE_SLEEP_DELAY * 1000L
		@JvmStatic
		val PREFIX = "--"
		val Commands: CommandPool by lazyOf(CommandPool())
		/* @formatter:on */

		interface Permission {
			companion object {
				val ANY = 0
				val MEMBER = 1 shl 2
				val ADMINISTRATOR = 1 shl 3
				val OWNER = 1 shl 4
				val DEV = 1 shl 5

				fun check(user: Member?, permission: Int = 0): Boolean {
					return when {
						user == null            -> false
						user.idLong in DEV_LIST -> true
						else                    -> when (permission) {
							permission and ANY           -> true
							permission and MEMBER        ->
								user.isOwner || user.hasPermission(net.dv8tion.jda.api.Permission.MESSAGE_WRITE)
							permission and ADMINISTRATOR ->
								user.isOwner || user.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR)
							permission and OWNER         -> user.isOwner
							permission and DEV           -> user.idLong in DEV_LIST
							else                         -> false
						}
					}
				}
			}
		}
	}
}