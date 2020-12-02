package com.wireless4024.discordbot.internal

import kotlin.math.max
import kotlin.math.min

/**
 * @see <a href="https://stackoverflow.com/a/11384393/10765376">https://stackoverflow.com/a/11384393/10765376</a>
 */
internal class PrettyPrinter private constructor(private val out: StringBuilder = StringBuilder(),
                                                 private val asNull: String = DEFAULT_AS_NULL) {

	override fun toString(): String {
		return out.toString()
	}

	fun print(table: Array<Array<out Any?>?>, lineLength: Int) {
		if (table.isEmpty()) return
		val widths = IntArray(getMaxColumns(table))
		adjustColumnWidths(table, widths, lineLength)
		printPreparedTable(table, widths, getHorizontalBorder(widths))
	}

	private fun printPreparedTable(table: Array<Array<out Any?>?>,
	                               widths: IntArray,
	                               horizontalBorder: String) {
		val lineLength = horizontalBorder.length
		out.appendln(horizontalBorder)
		for (row in table) {
			if (row != null) {
				out.appendln(getRow(row, widths, lineLength))
				out.appendln(horizontalBorder)
			}
		}
	}

	private fun getRow(row: Array<out Any?>,
	                   widths: IntArray,
	                   lineLength: Int): String {
		val builder: StringBuilder = StringBuilder(lineLength)
		val maxWidths = widths.size
		val col = Array<Array<String>>(maxWidths) { emptyArray() }
		for (i in 0 until maxWidths) col[i] = safeGet(row, i).chunked(widths[i]).toTypedArray()
		val lines = col.asSequence().map { it.size }.maxOrNull()!!
		val padding = if (lines > 1) Array(maxWidths) { " ".repeat(widths[it]) } else emptyArray()
		for (i in 0 until lines) {
			builder.append(VERTICAL_BORDER)
			for (j in 0 until maxWidths)
				builder.append(col[j].getOrNull(i)?.padEnd(widths[j]) ?: padding[j]).append(VERTICAL_BORDER)
			builder.append('\n')
		}
		return builder.dropLast(1).toString()
	}

	private fun getHorizontalBorder(widths: IntArray): String {
		val builder = StringBuilder(256)
		builder.append(BORDER_KNOT)
		for (w in widths) {
			for (i in 0 until w) {
				builder.append(HORIZONTAL_BORDER)
			}
			builder.append(BORDER_KNOT)
		}
		return builder.toString()
	}

	private fun getMaxColumns(rows: Array<Array<out Any?>?>): Int {
		var max = 0
		for (row in rows) {
			if (row != null && row.size > max) {
				max = row.size
			}
		}
		return max
	}

	private fun adjustColumnWidths(rows: Array<Array<out Any?>?>,
	                               widths: IntArray, lineLength: Int = 2147483647) {
		val cols = widths.size
		val colsize = (lineLength / cols.plus(1)).let { if (it * cols.plus(1) >= lineLength) it - 1 else it }
		val requires = IntArray(cols)
		for (row in rows) {
			if (row != null) {
				for (c in 0..widths.lastIndex) {
					val width = max(widths[c], getCellValue(safeGet(row, c, asNull)).length)
					requires[c] = max(requires[c], width)
					widths[c] = min(colsize, width)
				}
			}
		}
		var remaining = (min(requires.sum(), (lineLength - 1) - (cols)) - widths.sum())
		while (remaining > 0) {
			for (i in 0 until cols)
				if (remaining <= 0) break
				else if (widths[i] < requires[i]) {
					++widths[i]
					--remaining
				}
		}
	}

	@Suppress("NOTHING_TO_INLINE")
	private inline fun getCellValue(value: Any?): String {
		return (value?.toString() ?: asNull)
	}

	companion object {

		private const val BORDER_KNOT = '+'
		private const val HORIZONTAL_BORDER = '-'
		private const val VERTICAL_BORDER = '|'
		private const val DEFAULT_AS_NULL = "(NULL)"
		private fun padRight(s: String, n: Int): String {
			return s.padEnd(n)
		}

		private fun safeGet(
			array: Array<out Any?>,
			index: Int,
			defaultValue: String = DEFAULT_AS_NULL
		): String {
			return array.getOrNull(index)?.toString() ?: defaultValue
		}

		@JvmStatic
		fun format(value: Array<Array<out Any?>?>, lineLength: Int = 2147483647) = StringBuilder().also {
			PrettyPrinter(it).print(value, lineLength)
		}.toString()

		private fun StringBuilder.appendln(value: Any?) = this.append(value).append('\n')
	}
}