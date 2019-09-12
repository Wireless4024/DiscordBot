package com.wireless4024.discordbot.internal

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.wireless4024.discordbot.internal.music.Controller
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.Region.UNKNOWN
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Guild

class ConfigurationCache private constructor(var guild: Guild) {
	companion object {
		private val Cache = mutableMapOf<Long, ConfigurationCache>()

		fun get(guild: Guild): ConfigurationCache {
			if (!Cache.containsKey(guild.idLong))
				Cache[guild.idLong] = ConfigurationCache(guild)
			return Cache[guild.idLong]!!.update(guild)
		}
	}

	val audioPlayerManager = DefaultAudioPlayerManager().also { AudioSourceManagers.registerRemoteSources(it) }

	val musicController = Controller(this)

	var audioSendHandler: AudioSendHandler?
		get() = audioManager.sendingHandler
		set(value) = with(value) { audioManager.sendingHandler = this }

	val audioManager
		get() = guild.audioManager

	var Expressions = com.keelar.exprk.Expressions()

	fun closeAudioConnection() = audioManager.closeAudioConnection()

	fun update(guild: Guild? = null): ConfigurationCache = this.also {
		if (guild != null)
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