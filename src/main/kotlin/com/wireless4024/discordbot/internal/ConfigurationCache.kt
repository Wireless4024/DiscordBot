package com.wireless4024.discordbot.internal

import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.Region.UNKNOWN
import net.dv8tion.jda.api.entities.Guild

class ConfigurationCache private constructor(var guild: Guild) {
	companion object {
		private val Cache = mutableMapOf<Long, ConfigurationCache>()

		fun get(guild: Guild): ConfigurationCache {
			if (Cache.containsKey(guild.idLong))
				Cache[guild.idLong] = ConfigurationCache(guild)
			return Cache[guild.idLong]!!.update(guild)
		}
	}

	val audioManager
		get() = guild.audioManager

	fun closeAudioConnection() = audioManager.closeAudioConnection()

	fun update(guild: Guild): ConfigurationCache = this.also {
		this.guild = guild
	}

	fun ban(id: String, delay: Int = 0, reason: String = "") {
		guild.ban(id, delay, reason)
	}

	fun setRegion(where: String) {
		guild.manager.setRegion(Region.fromKey(where).also { require(it != UNKNOWN) { "invalid region key" } })
	}

	val Regions = Region.values()
}