package com.wireless4024.discordbot

import com.wireless4024.discordbot.internal.ConfigurationCache
import com.wireless4024.discordbot.internal.Property
import com.wireless4024.discordbot.internal.Utils
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDABuilder

fun main() {
	System.setProperty("idea.io.use.fallback", "true")
	try {
		Property.JDA = JDABuilder(AccountType.BOT)
			.setToken(Property.TOKEN)
			//.setAudioSendFactory(NativeAudioSendFactory())
			.addEventListeners(Utils.globalEvent)
			.build()
	} catch (e: Throwable) {
		Utils.error(e.message ?: e.toString())
	}
	ConfigurationCache.init()
}