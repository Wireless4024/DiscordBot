package com.wireless4024.discordbot.internal.music

import com.sedmelluq.discord.lavaplayer.format.OpusAudioDataFormat
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class AudioHandler(private val audioPlayer: AudioPlayer) : AudioSendHandler {
	private var lastFrame: AudioFrame? = null

	override fun canProvide(): Boolean = audioPlayer.provide().also { lastFrame = it } != null

	override fun provide20MsAudio(): ByteBuffer? =
			if (lastFrame == null || lastFrame!!.data == null) null else ByteBuffer.wrap(lastFrame!!.data)

	override fun isOpus(): Boolean = lastFrame != null && lastFrame!!.format is OpusAudioDataFormat
}