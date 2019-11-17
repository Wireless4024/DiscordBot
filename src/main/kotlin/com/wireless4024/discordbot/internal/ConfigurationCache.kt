package com.wireless4024.discordbot.internal

import com.keelar.exprk.Expressions
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration.ResamplingQuality.LOW
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.wireless4024.discordbot.internal.config.DiscordServer
import com.wireless4024.discordbot.internal.config.ExprkSetting
import com.wireless4024.discordbot.internal.config.MusicSetting
import com.wireless4024.discordbot.internal.config.Setting
import com.wireless4024.discordbot.internal.music.Controller
import com.wireless4024.discordbot.internal.rhino.JsExecutor
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Region
import net.dv8tion.jda.api.Region.UNKNOWN
import net.dv8tion.jda.api.audio.AudioSendHandler
import net.dv8tion.jda.api.entities.Guild
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.math.RoundingMode
import java.sql.Connection
import java.util.concurrent.atomic.AtomicBoolean

class ConfigurationCache private constructor(var guild: Guild, var lastEvent: MessageEvent? = null) {
	companion object {
		private val Cache = mutableMapOf<Long, ConfigurationCache>()

		fun get(guild: Guild, lastEvent: MessageEvent): ConfigurationCache {
			if (!Cache.containsKey(guild.idLong))
				Cache[guild.idLong] = ConfigurationCache(guild, lastEvent)
			return Cache[guild.idLong]!!.update(guild, lastEvent)
		}

		private val roundingMode = RoundingMode.values()

		fun init() {
			runBlocking {
				val db = KMongo.createClient().coroutine
				launch {
					db.getDatabase(Property.dbname)
						.getCollection<DiscordServer>("setting")
						.find().toList().forEach() { deserialize(it) }
				}
				kotlinx.coroutines.delay(30000)
				db.close()
			}
		}

		fun submit() {
			val db = KMongo.createClient().coroutine.getDatabase(Property.dbname)
				.getCollection<DiscordServer>("setting")
			runBlocking {
				Cache.forEach() { (_, it) -> launch { db.save(it.serialize()) } }
			}
		}

		fun deserialize(data: DiscordServer) {
			val guild = Property.JDA.getGuildById(data.guild) ?: return
			println("loading configuration for ${guild.name}")
			val it = ConfigurationCache(guild)
			Cache[data.guild] = it
			val setting = data.setting
			if (setting.exprk == null) setting.exprk = ExprkSetting()
			if (setting.music == null) setting.music = MusicSetting()
			it.prefix = setting.prefix
			it.Expressions = Expressions().also {
				it.setPrecision(setting.exprk!!.precision)
				it.setRoundingMode(roundingMode.first { it2 -> it2.name == setting.exprk!!.roundingMode })
				it.setVariables(setting.exprk!!.variables)
			}
			val music = it.musicController
			music.volume(setting.music!!.volume)
			music.deserializeQueue(setting.music!!.queues)
			val vc = if (setting.music!!.channel == 0L) null else guild.getVoiceChannelById(setting.music!!.channel)
			music.player().isPaused = false
			if (vc != null) {
				music.connect(it.audioManager, vc, true)
				music.player().isPaused = !setting.music!!.playing
			} else {
				music.player().isPaused = true
			}
			if (music.player().playingTrack != null)
				music.player().playingTrack.position = setting.music!!.position
		}
	}

	private val RunningTask = mutableMapOf<Any, AtomicBoolean>()

	operator fun get(key: Any) = (if (RunningTask.containsKey(key)) RunningTask[key]!!.get()
	else {
		RunningTask[key] = AtomicBoolean(false)
		RunningTask[key]!!.get()
	})

	operator fun set(key: Any, value: Boolean) = (if (RunningTask.containsKey(key)) RunningTask[key]!!.set(value)
	else RunningTask[key] = AtomicBoolean(value))

	val audioPlayerManager = DefaultAudioPlayerManager().also {
		AudioSourceManagers.registerRemoteSources(it)
		val configuration = it.configuration
		configuration.resamplingQuality = LOW
		configuration.opusEncodingQuality = 10
		configuration.isFilterHotSwapEnabled = true
	}

	val JavascriptEngine = JsExecutor(this)

	val FunctionX = functionx()

	val musicController = Controller(this)

	var prefix = Property.PREFIX

	var audioSendHandler: AudioSendHandler?
		get() = audioManager.sendingHandler
		set(value) = with(value) { audioManager.sendingHandler = this }

	val audioManager
		get() = guild.audioManager

	@Volatile var Expressions = Expressions()

	@Volatile var sqliteInstance: Connection? = null

	fun closeAudioConnection() = audioManager.closeAudioConnection()

	@Volatile private var context: MutableMap<Long, Context> = mutableMapOf()

	fun destroySQLiteInstance() {
		sqliteInstance?.close()
		sqliteInstance = null
	}

	fun runContext(evt: MessageEvent): Boolean = context.containsKey(evt.ch.idLong) && context[evt.ch.idLong]!!(evt)

	fun registerContext(name: String, id: Long, fnc: (MessageEvent) -> Any?, whenClosed: (MessageEvent) -> Unit = {}) {
		this.context[id] = Context(name, fnc) { context.remove(id);whenClosed(it) }
	}

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

	fun serialize(): DiscordServer {
		return DiscordServer(
			DiscordServer.translateObjectID(guild.idLong),
			guild.idLong,
			Setting(
				prefix,
				ExprkSetting(
					Expressions.precision,
					Expressions.roundingMode.name,
					Expressions.variables()
				),
				MusicSetting(
					musicController.player().volume,
					!musicController.player().isPaused,
					musicController.player().playingTrack?.position ?: 0,
					audioManager.connectedChannel?.idLong ?: 0,
					musicController.serializeQueue()
				)
			)
		)
	}
}