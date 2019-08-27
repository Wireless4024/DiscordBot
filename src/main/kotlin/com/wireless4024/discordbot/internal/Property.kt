package com.wireless4024.discordbot.internal

import com.keelar.exprk.Expressions
import net.dv8tion.jda.api.entities.Member

interface Property {
	companion object {
		val TOKEN = ""
		val LOGGER_NAME = "DiscordBot"
		val DEV_LIST = listOf(298273616704045057L)
		val PREFIX = "--"
		val Expressions = Expressions()
		val Commands = CommandPool()

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
							else                         -> false
						}
					}
				}
			}
		}
	}
}