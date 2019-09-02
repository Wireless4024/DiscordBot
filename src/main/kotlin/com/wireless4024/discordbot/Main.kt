package com.wireless4024.discordbot

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory
import com.wireless4024.discordbot.internal.Handler
import com.wireless4024.discordbot.internal.Property
import net.dv8tion.jda.api.AccountType
import net.dv8tion.jda.api.JDABuilder

fun main() {
	System.setProperty("idea.io.use.fallback", "true")

	JDABuilder(AccountType.BOT)
			.setToken(Property.TOKEN)
			.setAudioSendFactory(NativeAudioSendFactory())
			.addEventListeners(Handler())
			.build()
}