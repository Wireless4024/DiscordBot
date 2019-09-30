package com.wireless4024.discordbot.internal.rhino

import com.wireless4024.discordbot.internal.Utils
import org.apache.http.HttpResponse
import org.apache.http.client.methods.*
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.HttpClientBuilder
import org.mozilla.javascript.*
import java.io.InputStream
import java.net.URL
import java.net.URLEncoder
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

	fun className(value: Any?) = value?.javaClass?.simpleName ?: "null"
	fun fullClassName(value: Any?) = value?.javaClass?.name ?: "null"

	fun request(url: String, method: String, data: Any? = null): String {
		val target = URL(Utils.getFinalURL(if (url.startsWith("http")) url else "http://$url"))
		val contents: InputStream
		try {
			when {
				method.toUpperCase() in arrayOf("POST", "PUT")                               -> {
					val request = if (method.equals("POST", true)) HttpPost(target.toURI()) else HttpPut(target.toURI())
					request.addHeader("Content-Type", "application/x-www-form-urlencoded")
					request.addHeader("User-Agent", "curl") // fake curl
					request.addHeader("Cache-Control", "max-age=0") // do not cache
					request.entity = StringEntity(
						if (data !is NativeObject)
							if (data is String) data
							else ""
						else toUrlEncoded(data),
						Charsets.UTF_8
					)
					val response: HttpResponse = HttpClient.execute(request)
					contents = response.entity.content
				}
				method.toUpperCase() in arrayOf("GET", "HEAD", "OPTIONS", "DELETE", "TRACE") -> {
					val request: HttpUriRequest = when (method.toUpperCase()) {
						"HEAD"    -> HttpHead(target.toURI())
						"OPTIONS" -> HttpOptions(target.toURI())
						"DELETE"  -> HttpDelete(target.toURI())
						"TRACE"   -> HttpTrace(target.toURI())
						else      -> HttpGet(target.toURI())
					}
					request.addHeader("User-Agent", "curl") // fake curl
					request.addHeader("Cache-Control", "max-age=0") // do not cache
					val response: HttpResponse = HttpClient.execute(request)
					contents = response.entity.content
				}
				else                                                                         -> throw RuntimeException("method not supported")
			}
			// limit to 1903 character because if content is very large we shouldn't read them all cause wasting bandwidth/memory
			val sb = java.lang.StringBuilder(1903)
			var char = contents.read()
			while (char != -1 && sb.length < 1900) {
				sb.append(char.toChar())
				char = contents.read()
			}
			contents.close()
			if (sb.length == 1900)
				sb.append("...")
			return sb.toString()
		} catch (e: Throwable) {
			return e.toString()
		}
	}

	private fun trim(string: String) = trims.replace(trim.replace(string, ""), " ")

	private fun toString(value: Any?, replaceFunctionWithNull: Boolean = false): String {
		return when (value) {
			null                         -> "null"
			is java.util.Map.Entry<*, *> -> toQuotedString(value.key) + ":" + toQuotedString(value.value)
			is Array<*>                  -> deepToString(value)
			is NativeArray               -> deepToString(value.toArray())
			is BaseFunction              -> {
				if (replaceFunctionWithNull) "null"
				else trim(Context.enter().decompileFunction(value, 0))
			}
			is NativeObject              -> toJSON(value, replaceFunctionWithNull)
			is java.lang.Number          -> {
				val double = value.doubleValue()
				val long = double.toLong()
				if (double.absoluteValue <= 9007199254740991.0 && (double - long) <= 0.0) value.longValue().toString()
				else value.toString()
			}
			is NativeJavaObject          -> NativeJavaObject::class.java.getDeclaredField("javaObject").also {
				it.isAccessible = true
			}
				.get(value).toString()
			else                         -> value.toString()
		}
	}

	private fun toQuotedString(value: Any?, replaceFunctionWithNull: Boolean = false): String {
		return when (value) {
			null                -> "null"
			is Boolean          -> value.toString()
			is java.lang.Number -> {
				val double = value.doubleValue()
				val long = double.toLong()
				if (double.absoluteValue <= 9007199254740991.0 && (double - long) <= 0.0) value.longValue().toString()
				else value.toString()
			}
			is BaseFunction     -> {
				if (replaceFunctionWithNull) "null"
				else trim(Context.enter().decompileFunction(value, 0))
			}
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

	private fun toUrlEncoded(obj: NativeObject): String {
		val out = java.lang.StringBuilder(obj.size * 20)
		obj.forEach { key, value ->
			out.append(URLEncoder.encode(toString(key), "UTF-8"))
				.append('=')
				.append(URLEncoder.encode(toString(value, true), "UTF-8"))
				.append('&')
		}
		return out.toString().let { if (it.isNotEmpty()) it.dropLast(1) else it }
	}

	private fun toJSON(obj: NativeObject, funcToNull: Boolean): String {
		val out = java.lang.StringBuilder(obj.size * 20).append('{')
		obj.forEach { key, value ->
			out.append(toQuotedString(key)).append(':').append(toQuotedString(value, funcToNull))
		}
		return out.append('}').toString()
	}

	private fun deepToString(a: Array<*>?): String {
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

	internal fun collectSTDOUT(): String {
		val str = stdout.toString()
		stdout.clear()
		return str.trim().let { if (it.isEmpty()) "nothing return from your function" else it }
	}

	companion object {
		val INSTANCE = StandardLib()
		val HttpClient = HttpClientBuilder.create().build()
	}
}