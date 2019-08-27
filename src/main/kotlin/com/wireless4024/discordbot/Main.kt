package com.wireless4024.discordbot

import com.wireless4024.discordbot.command.kts
import com.wireless4024.discordbot.internal.Handler
import com.wireless4024.discordbot.internal.Property
import net.dv8tion.jda.api.JDABuilder

fun main() {
	// JDABuilder(Property.TOKEN).addEventListeners(Handler()).build()
	kts.callme()
}