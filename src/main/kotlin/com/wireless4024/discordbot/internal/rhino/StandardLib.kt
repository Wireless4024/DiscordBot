package com.wireless4024.discordbot.internal.rhino

import org.mozilla.javascript.BaseFunction
import org.mozilla.javascript.Context
import org.mozilla.javascript.NativeArray
import org.mozilla.javascript.NativeObject
import java.util.*
import kotlin.math.absoluteValue

class StandardLib {
	val trim = Regex("[\r\n\t]+")
	val trims = Regex("\\s+")
	val stdout = StringBuilder()

	fun print(value: Any?) {
		stdout.append(toString(value))
	}

	fun println(value: Any?) {
		stdout.append(value).append('\n')
	}

	private fun trim(string: String) = trims.replace(trim.replace(string, ""), " ")

	private fun toString(value: Any?): String {
		return when (value) {
			null                         -> "null"
			is java.util.Map.Entry<*, *> -> toQuotedString(value.key) + ":" + toQuotedString(value.value)
			is Array<*>                  -> deepToString(value)
			is NativeArray               -> deepToString(value.toArray())
			is BaseFunction              -> trim(Context.enter().decompileFunction(value, 0))
			is NativeObject              -> toJSON(value)
			is java.lang.Number          -> {
				if (value.doubleValue().absoluteValue <= 9007199254740991.0) value.longValue().toString()
				else value.toString()
			}
			else                         -> value.toString()
		}
	}

	private fun toQuotedString(value: Any?): String {
		return when (value) {
			null                -> "null"
			is java.lang.Number -> {
				if (value.doubleValue().absoluteValue <= 9007199254740991.0) value.longValue().toString()
				else value.toString()
			}
			is BaseFunction     -> trim(Context.enter().decompileFunction(value, 0))
			else                -> quote(toString(value))
		}
	}

	private fun quote(value: String): String {
		val product = java.lang.StringBuilder().append("\"")
		for (ch in value.toCharArray()) {
			when (ch) {
				'\\'     -> product.append("\\\\")
				'"'      -> product.append("\\\"")
				'\b'     -> product.append("\\b")
				'\u000c' -> product.append("\\f")
				'\n'     -> product.append("\\n")
				'\r'     -> product.append("\\r")
				'\t'     -> product.append("\\t")
				else     -> {
					if (ch < ' ')
						product.append(String.format("\\u%04x", ch.toInt()))
					else
						product.append(ch)
				}
			}
		}
		return product.append("\"").toString()
	}

	private fun toJSON(obj: NativeObject): String {
		val out = java.lang.StringBuilder(obj.size * 20).append('{')
		obj.forEach { key, value -> out.append(toQuotedString(key)).append(':').append(toQuotedString(value)) }
		return out.append('}').toString()
	}

	fun deepToString(a: Array<*>?): String {
		if (a == null) return "null"
		var bufLen = 20 * a.size
		if (a.isNotEmpty() && bufLen <= 0) bufLen = Integer.MAX_VALUE
		val buf = java.lang.StringBuilder(bufLen)
		deepToString(a, buf, HashSet())
		return buf.toString()
	}

	@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
	private fun deepToString(a: Array<*>?, buf: java.lang.StringBuilder, dejaVu: HashSet<Array<*>>) {
		if (a == null) {
			buf.append("null")
			return
		}
		val iMax = a.size - 1
		if (iMax == -1) {
			buf.append("[]")
			return
		}
		dejaVu.plus(a)
		buf.append('[')
		var i = 0
		while (true) {
			val element = a[i]
			if (element == null) {
				buf.append("null")
			} else {
				val eClass: Class<*> = element.javaClass
				if (eClass.isArray) {
					when (eClass) {
						ByteArray::class.java    -> buf.append((element as ByteArray).contentToString())
						ShortArray::class.java   -> buf.append((element as ShortArray).contentToString())
						IntArray::class.java     -> buf.append((element as IntArray).contentToString())
						LongArray::class.java    -> buf.append((element as LongArray).contentToString())
						CharArray::class.java    -> buf.append((element as CharArray).contentToString())
						FloatArray::class.java   -> buf.append((element as FloatArray).contentToString())
						DoubleArray::class.java  -> buf.append((element as DoubleArray).contentToString())
						BooleanArray::class.java -> buf.append((element as BooleanArray).contentToString())
						else                     -> // element is an array of object references
							if (dejaVu.contains(element)) buf.append("[...]")
							else deepToString(element as Array<*>, buf, dejaVu)
					}
				} else {  // element is non-null and not an array
					buf.append(toQuotedString(element))
				}
			}
			if (i == iMax) break
			buf.append(", ")
			i++
		}
		buf.append(']')
		dejaVu.remove(a)
	}

	fun collectSTDOUT(): String {
		val str = stdout.toString()
		stdout.clear()
		return str.trim().let { if (it.isEmpty()) "nothing return from your function" else it }
	}
}