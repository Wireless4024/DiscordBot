package com.wireless4024.discordbot.internal.music
/*

import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.youtube.YouTube
import com.wireless4024.discordbot.internal.Property
import kotlin.math.min

class YoutubeAPI {
	companion object {
		@JvmStatic
		val youtube = YouTube.Builder(NetHttpTransport(), JacksonFactory()) {}
			.also { it.applicationName = "DiscordBotV2" }.build()

		@JvmStatic
		fun search(word: String, limit: Long) {
			youtube.search().list("id,snippet").also {
				it.key = Property.YTTOKEN
				it.q = word
				it.type = "video"
				it.fields = "items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url)"
				it.maxResults = min(limit, 10)
			}
		}

		@JvmStatic
		fun pickFirst(word: String): String {
			return youtube.search().list("id").also {
				it.key = Property.YTTOKEN
				it.q = word
				it.type = "video"
				it.fields = "items(id/videoId)"
				it.maxResults = 1
			}.execute().items.let { if (it.size < 1) word else it[0].id.videoId }
		}
	}
}*/
