package com.wireless4024.discordbot.internal.config

import com.keelar.exprk.Expressions
import org.bson.types.ObjectId
import java.math.BigDecimal
import java.nio.ByteBuffer

data class DiscordServer(
	val _id: ObjectId,
	val guild: Long,
	val setting: Setting,
	val version: Long = 1
) {

	companion object {
		@JvmStatic
		fun translateLong(id: ObjectId): Long {
			return ByteBuffer.wrap(id.toByteArray()).getLong(0)
		}

		@JvmStatic
		fun translateObjectID(long: Long): ObjectId {
			val bytes = ByteArray(12)
			return ObjectId(ByteBuffer.wrap(bytes).also {
				it.putLong(0, long)
				it.putInt(8, long.toInt())
			}.array())
		}

		@JvmStatic
		fun createTest(num: Int): DiscordServer {
			return DiscordServer(
				translateObjectID(num.toLong()),
				num.toLong(),
				Setting(
					"$",
					ExprkSetting(),
					MusicSetting()
				)
			)
		}
	}
}

data class Setting(
	var prefix: String = "--",
	var exprk: ExprkSetting? = ExprkSetting(),
	var music: MusicSetting? = MusicSetting()
) {

	fun updateExprk(exprk: Expressions): Setting {
		val newExprk = ExprkSetting(exprk.precision, exprk.roundingMode.toString(), exprk.variables())
		return Setting(prefix, newExprk, music)
	}

	fun updateMusic(newMusic: MusicSetting): Setting {
		return Setting(prefix, exprk, newMusic)
	}
}

data class ExprkSetting(
	val precision: Int = 128,
	val roundingMode: String = "FLOOR",
	val variables: List<Pair<String, BigDecimal>> = emptyList() // variable pair Pair(name, value)
)

@Suppress("ArrayInDataClass")
data class MusicSetting(
	val volume: Int = 100,
	val playing: Boolean = false,
	val position: Long = 0,
	val channel: Long = 0, // 0 if not connected
	val queues: Array<String> = emptyArray()
)