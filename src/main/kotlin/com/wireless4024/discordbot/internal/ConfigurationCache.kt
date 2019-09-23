package com.wireless4024.discordbot.internal

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality.LOW
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.wireless4024.discordbot.internal.music.Controller
import com.wireless4024.discordbot.internal.rhino.JsExecutor
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.Region.UNKNOWN
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Guild

class ConfigurationCache private constructor(var guild: Guild, var lastEvent: MessageEvent? = null) {
	companion object {
		private val Cache = mutableMapOf<Long, ConfigurationCache>()

		fun get(guild: Guild, lastEvent: MessageEvent): ConfigurationCache {
			if (!Cache.containsKey(guild.idLong))
				Cache[guild.idLong] = ConfigurationCache(guild, lastEvent)
			return Cache[guild.idLong]!!.update(guild, lastEvent)
		}
	}

	val audioPlayerManager = DefaultAudioPlayerManager().also {
		AudioSourceManagers.registerRemoteSources(it)
		val configuration = it.configuration
		configuration.resamplingQuality = LOW
		configuration.opusEncodingQuality = 10
		configuration.isFilterHotSwapEnabled = true
	}

	val JavascriptEngine = JsExecutor(this)

	val musicController = Controller(this)

	var prefix = Property.PREFIX

	var audioSendHandler: AudioSendHandler?
		get() = audioManager.sendingHandler
		set(value) = with(value) { audioManager.sendingHandler = this }

	val audioManager
		get() = guild.audioManager

	var Expressions = com.keelar.exprk.Expressions()

	fun closeAudioConnection() = audioManager.closeAudioConnection()

	fun update(guild: Guild? = null, event: MessageEvent): ConfigurationCache = this.also {
		if (guild != null)
			this.guild = guild
		lastEvent = event
	}

	fun ban(id: String, delay: Int = 0, reason: String = "") {
		guild.ban(id, delay, reason)
	}

	fun setRegion(where: String) {
		guild.manager.setRegion(Region.fromKey(where).also { require(it != UNKNOWN) { "invalid region key" } })
	}

	val Regions = Region.values()
}