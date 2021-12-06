package com.wireless4024.discordbot.internal

import com.keelar.exprk.Expressions
import com.keelar.exprk.internal.DefaultEvaluator
import com.wireless4024.discordbot.internal.functionx.FunctionX
import java.io.EOFException
import java.math.BigDecimal

class functionx {
	private val ev = Expressions(true, 64)
	/* synchronized seem not enough to lock an Expression */
	private val lock = java.util.concurrent.locks.ReentrantLock()

	operator fun invoke(func: String, args: List<BigDecimal>): String {
		lock.lock()
		registerVariables(ev, args, false)
		val result = ev.evalToString(func)
		lock.unlock()
		return result
	}

	operator fun invoke(func: String): String = invoke(Scanner(func).scan().get())

	operator fun invoke(func: FunctionX): String {
		lock.lock()
		registerVariables(ev, func.params)
		val result = ev.evalToString(func.func)
		lock.unlock()
		return result
	}

	data class FunctionX(val params: List<Pair<String?, BigDecimal?>>, val func: String)
	companion object {

		val FXRegex = Regex("f\\([^)]\\)=.+")

		fun variableNameAt(index: Int) = when (index) {
			0    -> 'x'
			1    -> 'y'
			2    -> 'z'
			else -> 'a'.toInt().plus(index - 3).toChar()
		}.toString()

		fun registerVariables(ev: Expressions, params: List<Pair<String?, BigDecimal?>>) {
			ev.reset()
			val variables = ev.evaluator.variables
			var count = 0
			if (params.size >= 27) throw UnsupportedOperationException("arguments too long")
			params.forEach { (key, value) ->
				var k = key ?: variableNameAt(count++)
				while (variables.containsKey(k))
					k = variableNameAt(count++)
				variables[k] = value ?: DefaultEvaluator.ZERO
			}
		}

		fun registerVariables(ev: Expressions, params: List<BigDecimal>, ignored: Boolean) {
			ev.reset()
			val variables = ev.evaluator.variables
			var count = 0
			if (params.size >= 27) throw UnsupportedOperationException("arguments too long")
			for (value in params) {
				var k = variableNameAt(count++)
				while (variables.containsKey(k))
					k = variableNameAt(count++)
				variables[k] = value
			}
		}
	}
}

private class Scanner(val input: String) {
	private val result = mutableListOf<Pair<String?, BigDecimal?>>()
	var done = false
	var pos = input.indexOf('(')

	fun nextNumber() {
		val form = pos
		var to = form
		while (input[pos] != ',' && com.keelar.exprk.internal.Scanner.isCorrectBigDecimalSyntax(
				input[pos],
				input.getOrNull(pos - 1) ?: '\u0000',
				input.getOrNull(pos + 1) ?: '\u0000'
			)
		) {
			to += 1;pos += 1
		}
		result.add(null to input.substring(form, to).parseBigDecimal())
	}

	fun nextVariable() {
		var form = pos
		var to = form
		while (input[pos] != '=') {
			to += 1;pos += 1
		}
		val name = input.substring(form, to).trim()
		form = to + 1
		to += 1
		pos += 1
		while (input[pos] != ',' && com.keelar.exprk.internal.Scanner.isCorrectBigDecimalSyntax(
				input[pos],
				input.getOrNull(pos - 1) ?: '\u0000',
				input.getOrNull(pos + 1) ?: '\u0000'
			)
		) {
			to += 1;pos += 1
		}
		result.add(name to input.substring(form, to).parseBigDecimal())
	}

	fun scan(): Scanner {
		if (done) return this
		var next = true
		do {
			when (input[++pos]) {
				'f', '(', ')', ' '       -> {
					// skip f()
				}
				'='                      -> next = input.hasBefore('(', pos)
				in '0'..'9', '.'         -> nextNumber()
				in 'a'..'z', in 'A'..'Z' -> nextVariable()
			}
			if (pos == input.length - 1) throw EOFException("end of expression, missing '='")
		} while (next && !(input[pos + 1] == ')' && input[pos + 1] == '='))
		done = true
		return this
	}

	fun get() = FunctionX(result, input.substring(pos + 1, input.length))
}