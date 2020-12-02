package com.wireless4024.discordbot

import com.wireless4024.discordbot.internal.ConfigurationCache
import com.wireless4024.discordbot.internal.Handler
import com.wireless4024.discordbot.internal.Property
import com.wireless4024.discordbot.internal.Utils
import net.dv8tion.jda.api.JDABuilder
import kotlin.system.exitProcess

fun main() {
	System.setProperty("idea.io.use.fallback", "true")
	try {
		Class.forName("com.wireless4024.discordbot.internal.Property")
		Property.JDA = JDABuilder.createDefault(Property.TOKEN)
			//.setToken()
			//.setAudioSendFactory(NativeAudioSendFactory())
			.addEventListeners(Handler.instance)
			.build()
	} catch (e: Throwable) {
		Utils.error(e.message ?: e.toString())
		exitProcess(1)
	}
	println("mongo error may show but not affected the bot")
	ConfigurationCache.init()
}