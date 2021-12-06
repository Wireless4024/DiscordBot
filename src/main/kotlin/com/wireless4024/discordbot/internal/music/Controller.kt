package com.wireless4024.discordbot.internal.music

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.wireless4024.discordbot.internal.*
import com.wireless4024.discordbot.internal.Property.Companion.ApplicationScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.VoiceChannel
import net.dv8tion.jda.api.managers.AudioManager
import java.awt.Color
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.math.max
import kotlin.math.min

class Controller(val parent: ConfigurationCache) {
    private val BASS_BOOST = floatArrayOf(.15f, .1f, .1f, .05f, .0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, .0f, .0f, .0f)
    private val BASS_BOOST2 = floatArrayOf(.3f, .25f, .2f, .15f, .1f, .5f, 0f, 0f, 0f, 0f, 0f, 0f, .1f, .2f, .25f)
    private var player: AudioPlayer
    private var manager: AudioPlayerManager = parent.audioPlayerManager
    private var bassBoosted = false
    private val bassboost: EqualizerFactory = EqualizerFactory().also {
        BASS_BOOST.forEachIndexed { index, value -> it.setGain(index, value) }
    }
    private val bassboost2: EqualizerFactory = EqualizerFactory().also {
        BASS_BOOST2.forEachIndexed { index, value -> it.setGain(index, value) }
    }
    private val scheduler: Scheduler

    init {
        player = manager.createPlayer()
        parent.audioSendHandler = AudioHandler(player)

        scheduler = Scheduler(player, this)
        player.addListener(scheduler)
    }

    fun queue(msgEV: MessageEvent) {
        val text = msgEV.msg
        if (text.isEmpty()) throw CommandError(if (pause(msgEV)) "resume playing" else "player paused")
        if (Utils.urlExisted(text))
            addTrack(text, msgEV)
        else
            addTrack(if (text.startsWith("ytsearch:")) text else "ytsearch:$text", msgEV, !text.startsWith("ytsearch:"))
    }

    fun join(msgEV: MessageEvent) = connect(parent.audioManager,msgEV.member.voiceState?.channel as VoiceChannel, true)

    fun leave() =
        with(parent.audioManager) {
            (this.connectedChannel?.name).also {
                player.isPaused = true;this.closeAudioConnection()
            }
        }
            ?: throw CommandError("wait dude I i can't leave I MOST BE IN VOICE CHANNEL TO USE THIS COMMAND!")

    fun skip() = scheduler.skip()

    fun clear() = scheduler.clear()

    fun volume(vol: Int): Int {
        if (vol <= 0)
            player.volume += vol
        else
            player.volume = vol
        return player.volume
    }

    fun listAsEmbed(page: Int = 1): MessageEmbed {
        val queue = scheduler.queues
        return EmbedBuilder().also {
            it.setTitle("Song queues | page $page")
            it.setDescription(
                "${queue.size} song in queue | duration ${Utils.toReadableFormatTime(scheduler.queueDuation)} | " +
                        "remaining ${Utils.toReadableFormatTime(scheduler.queueRemaining)}"
            )
            it.setColor(Color.GREEN)
            var position = (page - 1) * 10
            for (i in queue.safePartition(page))
                it.addField(
                    "${++position}. ${i.info.title}",
                    "duration : " + Utils.toReadableFormatTime(i.duration),
                    false
                )
        }.build()
    }

    private inline fun <reified T> Array<T>.safePartition(page: Int, size: Int = 10): Array<T> {
        val min = (page - 1) * size
        val end = min(min + size, this.size)
        if (min >= this.size)
            return emptyArray()
        return this.sliceArray(min until end)
    }

    fun player() = player

    fun bassBoost(level: Int?): Boolean {
        bassBoosted = if (bassBoosted && (level == null || level == 0)) {
            player.setFilterFactory(null)
            false
        } else {
            if (level == 2) player.setFilterFactory(bassboost2)
            else player.setFilterFactory(bassboost)
            true
        }
        return bassBoosted
    }

    fun forward(duration: Int) = playing { it.position = it.position + duration;it.position }!!

    fun back(duration: Int) = playing { it.position = max(0, it.position - duration); it.position }!!

    fun pause(event: MessageEvent) = player.isPaused.also {
        player.isPaused = !it
        if (it && (player.playingTrack != null || scheduler.size() > 0)) {
            connect(parent.audioManager, event.member.voiceState?.channel as VoiceChannel)
            player.isPaused = false
        }
        attemptToLeave()
    }

    fun repeat(mode: String = "") = scheduler.repeat(mode)

    fun removeQueue(pos: Int) = scheduler.remove(pos)

    val duration get() = player.playingTrack?.duration ?: 0

    fun seek(position: Long): Long {
        return playing {
            it.position = position
            it.position
        }!!
    }

    fun pos() = playing { "Position is " + it.position }!!

    fun previous(channel: VoiceChannel?): String? {
        connect(parent.audioManager, channel)
        return scheduler.previous()
    }

    fun attemptToLeave() = ApplicationScope.launch {
        if (parent["leaving"])
            return@launch
        parent["leaving"] = true
        delay(Property.LONG_TIMEOUT_MILLI)
        if (player.isPaused || (player.playingTrack == null && (scheduler.size() == 0)))
            parent.audioManager.closeAudioConnection()
        parent["leaving"] = false
    }

    private fun <E> List<E>.limit(count: Int) = this.subList(0, min(count - 1, this.size - 1))

    fun search(word: String, event: MessageEvent) {
        manager.loadItem("ytsearch:$word", object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                event.reply = EmbedBuilder().also {
                    it.setTitle(track.info.title)
                    it.setDescription(track.info.author)
                    it.setColor(Color.GREEN)
                    it.addField("url", track.info.uri, false)
                    it.addField("duration", Utils.toReadableFormatTime(track.info.length), false)
                }.build()
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val tracks = playlist.tracks
                event.reply = EmbedBuilder().also {
                    it.setTitle("Found ${tracks.size} tracks")
                    it.setColor(Color.GREEN)
                    tracks.forEach { track ->
                        it.addField(
                            track.info.title + " " + track.info.uri,
                            "by: ${track.info.author} | duration: ${
                                Utils.toReadableFormatTime(
                                    track.info.length
                                )
                            }", false
                        )
                    }
                }.build()
            }

            override fun noMatches() {
                ApplicationScope.launch { event.reply("Nothing found for $word") }
            }

            override fun loadFailed(throwable: FriendlyException) {
                ApplicationScope.launch { event.reply("Failed with message: " + throwable.message + " (" + throwable.javaClass.simpleName + ")") }
            }
        })
    }

    /**
     * @param pickfirst Boolean if search result ticked as playlist should player add song as single track
     */
    private fun addTrack(url: String, event: MessageEvent, pickfirst: Boolean = false) {
        manager.loadItemOrdered(this, url, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                connect(parent.audioManager, event.member.voiceState?.channel as VoiceChannel)
                ApplicationScope.launch {
                    if (scheduler.size() == 0 && player.playingTrack == null)
                        event.reply("now playing: ${track.info.title} (length ${Utils.toReadableFormatTime(track.duration)})")
                    else
                        event.reply("added ${track.info.title} (length ${Utils.toReadableFormatTime(track.duration)}) to queue")
                }
                scheduler.addToQueue(track)
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                connect(parent.audioManager, event.member.voiceState?.channel as VoiceChannel)

                if (pickfirst)
                    return this.trackLoaded(playlist.selectedTrack ?: playlist.tracks.let {
                        it.limit(3).firstOrNull {
                            it.info.title.contains("audio", true) &&
                                    !it.info.title.contains("live", true) &&
                                    !it.info.title.contains("performance", true)
                        } ?: it[0]
                    })

                val tracks = playlist.tracks
                var len = 0
                var duration = 0L
                tracks.slice(
                    (if (playlist.selectedTrack == null) 0 else tracks.indexOf(playlist.selectedTrack))..tracks.lastIndex
                ).forEach { ++len;duration += it.duration;scheduler.addToQueue(it) }.also {
                    event.reply = "added $len tracks duration ${Utils.toReadableFormatTime(duration)}"
                }
            }

            override fun noMatches() {
                ApplicationScope.launch { event.reply("Nothing found for $url") }
            }

            override fun loadFailed(throwable: FriendlyException) {
                ApplicationScope.launch { event.reply("Failed with message: " + throwable.message + " (" + throwable.javaClass.simpleName + ")") }
            }
        })
    }

    internal fun serializeQueue() = scheduler.getQueue().map {
        val out = ByteArrayOutputStream()
        manager.encodeTrack(MessageOutput(out), it)
        Base64.getEncoder().encodeToString(out.toByteArray())
    }.toTypedArray()

    internal fun deserializeQueue(data: Array<String>) =
        scheduler.loadQueue(data.map {
            manager.decodeTrack(MessageInput(ByteArrayInputStream(Base64.getDecoder().decode(it)))).decodedTrack
        }.toTypedArray())

    private inline fun <T> playing(operation: ((AudioTrack) -> T?)): T? =
        with(player.playingTrack ?: throw CommandError("player is not playing")) {
            operation(this)
        }

    internal fun connect(audioManager: AudioManager, voiceChannel: VoiceChannel?, force: Boolean = false): String {
        if (voiceChannel == null)
            throw CommandError("You must be in voice channel to use command")
        if (force || !audioManager.isConnected /*&& !audioManager.isAttemptingToConnect*/)
            audioManager.openAudioConnection(voiceChannel)
        return voiceChannel.name
    }
}