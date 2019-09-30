package com.wireless4024.discordbot

import com.wireless4024.discordbot.internal.ConfigurationCache
import com.wireless4024.discordbot.internal.Property
import com.wireless4024.discordbot.internal.Utils
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDABuilder
import kotlin.system.exitProcess

fun main() {
	System.setProperty("idea.io.use.fallback", "true")
	try {
		Property.JDA = JDABuilder(AccountType.BOT)
			.setToken(Property.TOKEN)
			//.setAudioSendFactory(NativeAudioSendFactory())
			.addEventListeners(Utils.globalEvent)
			.build()
	} catch (e: Throwable) {
		e.printStackTrace()
		exitProcess(-1)
	}
	ConfigurationCache.init()
}