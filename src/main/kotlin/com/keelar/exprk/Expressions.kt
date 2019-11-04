package com.keelar.exprk

import ch.obermuhlner.math.big.BigDecimalMath
import com.keelar.exprk.internal.*
import com.wireless4024.discordbot.internal.CommandError
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.security.SecureRandom
import kotlin.math.max
import kotlin.math.min

class ExpressionException(message: String) : RuntimeException(message)

@Suppress("unused")
class Expressions @JvmOverloads constructor(private val extended: Boolean = true) {

	private val evaluator: Evaluator = if (extended) ExtendedEvaluator() else DefaultEvaluator()

	init {
		define("pi", BigDecimalMath.pi(evaluator.context), true)
		define("e", BigDecimalMath.e(evaluator.context), true)

		evaluator.addFunction("abs") {
			if (it.size != 1) throw ExpressionException("abs requires one argument")
			it.first().abs()
		}

		evaluator.addFunction("sum") {
			if (it.isEmpty()) throw ExpressionException("sum requires at least one argument")
			it.reduce { sum, bigDecimal -> sum.add(bigDecimal) }
		}

		evaluator.addFunction("sumn") {
			if (it.isEmpty()) throw ExpressionException("sumn requires at least one argument")
			val n = it.first().toBigInteger()
			BigDecimal(n.add(BigInteger.ONE).multiply(n).shiftRight(1)).round(evaluator.context)
		}

		evaluator.addFunction("floor") {
			if (it.size != 1) throw ExpressionException("floor requires one argument")
			it.first().setScale(0, RoundingMode.FLOOR)
		}

		evaluator.addFunction("ceil") {
			if (it.size != 1) throw ExpressionException("ceil requires one argument")
			it.first().setScale(0, RoundingMode.CEILING)
		}

		evaluator.addFunction(arrayOf("fac", "factorial")) {
			if (it.size != 1) throw ExpressionException("factorial requires one argument")
			BigDecimalMath.factorial(it.first(), evaluator.context)
		}

		evaluator.addFunction(arrayOf("sqr", "square")) {
			if (it.size != 1) throw ExpressionException("square requires one argument")
			it.first().multiply(it.first())
		}

		evaluator.addFunction("cube") {
			if (it.size != 1) throw ExpressionException("cube requires one argument")
			it.first().multiply(it.first()).multiply(it.first())
		}

		evaluator.addFunction(arrayOf("sqrt", "squareroot")) {
			if (it.size != 1) throw ExpressionException("sqrt requires one argument")
			BigDecimalMath.sqrt(it.first(), evaluator.context)
		}

		evaluator.addFunction("gamma") {
			if (it.size != 1) throw ExpressionException("gamma requires one argument")
			BigDecimalMath.gamma(it.first(), evaluator.context)
		}

		evaluator.addFunction("log") {
			if (it.size != 1) throw ExpressionException("log requires one argument")
			BigDecimalMath.log(it.first(), evaluator.context)
		}

		evaluator.addFunction("log2") {
			if (it.size != 1) throw ExpressionException("log2 requires one argument")
			BigDecimalMath.log2(it.first(), evaluator.context)
		}

		evaluator.addFunction("log10") {
			if (it.size != 1) throw ExpressionException("log10 requires one argument")
			BigDecimalMath.log10(it.first(), evaluator.context)
		}

		evaluator.addFunction("asin") {
			if (it.size != 1) throw ExpressionException("asin requires one argument")
			BigDecimalMath.asin(it.first(), evaluator.context)
		}

		evaluator.addFunction("asinh") {
			if (it.size != 1) throw ExpressionException("asinh requires one argument")
			BigDecimalMath.asinh(it.first(), evaluator.context)
		}

		evaluator.addFunction("sin") {
			if (it.size != 1) throw ExpressionException("sin requires one argument")
			BigDecimalMath.sin(it.first(), evaluator.context)
		}

		evaluator.addFunction("sinh") {
			if (it.size != 1) throw ExpressionException("sinh requires one argument")
			BigDecimalMath.sinh(it.first(), evaluator.context)
		}

		evaluator.addFunction("acos") {
			if (it.size != 1) throw ExpressionException("acos requires one argument")
			BigDecimalMath.acos(it.first(), evaluator.context)
		}

		evaluator.addFunction("acosh") {
			if (it.size != 1) throw ExpressionException("acosh requires one argument")
			BigDecimalMath.acosh(it.first(), evaluator.context)
		}

		evaluator.addFunction("cos") {
			if (it.size != 1) throw ExpressionException("cos requires one argument")
			BigDecimalMath.cos(it.first(), evaluator.context)
		}

		evaluator.addFunction("cosh") {
			if (it.size != 1) throw ExpressionException("cosh requires one argument")
			BigDecimalMath.cosh(it.first(), evaluator.context)
		}

		evaluator.addFunction("atan") {
			if (it.size != 1) throw ExpressionException("atan requires one argument")
			BigDecimalMath.atan(it.first(), evaluator.context)
		}

		evaluator.addFunction("atanh") {
			if (it.size != 1) throw ExpressionException("atanh requires one argument")
			BigDecimalMath.atanh(it.first(), evaluator.context)
		}

		evaluator.addFunction("tan") {
			if (it.size != 1) throw ExpressionException("tan requires one argument")
			BigDecimalMath.tan(it.first(), evaluator.context)
		}

		evaluator.addFunction("tanh") {
			if (it.size != 1) throw ExpressionException("tanh requires one argument")
			BigDecimalMath.tanh(it.first(), evaluator.context)
		}

		evaluator.addFunction(arrayOf("fib", "fibonacci")) {
			if (it.size != 1) throw ExpressionException("fibonacci requires one argument")
			BigDecimal(fib(it.first().toInt())).round(evaluator.context)
		}

		evaluator.addFunction("root") {
			if (it.size < 2) throw ExpressionException("root requires two argument")
			BigDecimalMath.root(it.first(), it[1], evaluator.context)
		}

		evaluator.addFunction(arrayOf("rand", "random")) {
			if (it.isEmpty()) throw ExpressionException("random requires at least one argument")
			random(
				it[0].toBigInteger(),
				it.getOrNull(1)?.toBigInteger() ?: BigInteger.ZERO,
				it.getOrNull(2)?.toInt() ?: 0,
				it.getOrNull(3)?.toInt() ?: 0
			)
		}

		evaluator.addFunction(arrayOf("gauss", "gaussian")) {
			gaussian(it.getOrNull(0)?.toInt() ?: 8)
		}

		evaluator.addFunction("round") {
			if (it.size !in listOf(1, 2)) throw ExpressionException(
				"round requires either one or two it"
			)

			val value = it.first()
			val scale = if (it.size == 2) it.last().toInt() else 0

			value.setScale(scale, roundingMode)
		}

		evaluator.addFunction("min") {
			if (it.isEmpty()) throw ExpressionException("min requires at least one argument")
			it.min()!!
		}

		evaluator.addFunction("max") {
			if (it.isEmpty()) throw ExpressionException("max requires at least one argument")
			it.max()!!
		}

		evaluator.addFunction("if") {
			if (it[0].signum() != 0) it[1] else it[2]
		}

		evaluator.addFunction("decimal") {
			when (it.size) {
				1    -> it[0]
				2    -> {
					val scale = it[0].scale() + it[1].intValueExact()
					BigDecimal(it[0].unscaledValue(), scale)
				}
				else -> throw ExpressionException("decimal requires at least one argument")
			}
		}
	}

	val precision: Int
		get() = evaluator.context.precision

	val roundingMode: RoundingMode
		get() = evaluator.context.roundingMode

	val hundred: BigInteger = BigInteger.TEN.pow(2)

	private fun gaussian(digits: Int): BigDecimal {
		val digit = if (digits > precision) precision else max(digits, 2)
		val low = BigInteger.TEN.pow(digit - 2)
		println("min :$low")
		println("max :${low.multiply(hundred) - BigInteger.ONE}")
		return BigDecimal(random(low.multiply(hundred) - BigInteger.ONE, low), digit)
	}

	private fun random(
		max: BigInteger,
		min: BigInteger = BigInteger.ZERO,
		scaleMin: Int = 0,
		scaleMax: Int = 0
	): BigDecimal {
		val (mx, mn) = if (max > min) max to min else min to max
		val (smx, smn) = if (scaleMax > scaleMin) scaleMax to scaleMin else scaleMin to scaleMax
		return BigDecimal(
			if (mn.signum() == 0) random(mx) else random(mx - mn + BigInteger.ONE) + mn,
			random(smx, smn)
		)
	}

	private fun random(max: Int, min: Int): Int {
		return if (min == max) max else SecureRandom().nextInt(max - min) + min
	}

	private fun random(max: BigInteger, min: BigInteger = BigInteger.ZERO): BigInteger {
		return if (min.signum() == 0) random(max) else random(max - min) + min
	}

	private fun random(max: BigInteger): BigInteger {
		val rnd = SecureRandom()
		var randomNumber: BigInteger
		do randomNumber = BigInteger(max.bitLength(), rnd) while (randomNumber >= max)
		return randomNumber
	}

	private fun fib(n: Int): BigInteger {
		var a: BigInteger = BigInteger.ZERO
		var b: BigInteger = BigInteger.ONE
		var c: BigInteger
		for (j in 1..n) {
			c = a.add(b)
			a = b
			b = c
		}
		return a
	}

	fun setPrecision(precision: Int): Expressions {
		if (precision == -1)
			return this

		if (precision == 0)
			throw CommandError("infinity precision now allowed")

		evaluator.context = MathContext(min(precision, 1950), roundingMode)

		define("pi", BigDecimalMath.pi(evaluator.context), true)
		define("e", BigDecimalMath.e(evaluator.context), true)

		return this
	}

	fun setRoundingMode(roundingMode: RoundingMode): Expressions {
		evaluator.context = MathContext(precision, roundingMode)

		define("pi", BigDecimalMath.pi(evaluator.context), true)
		define("e", BigDecimalMath.e(evaluator.context), true)

		return this
	}

	fun define(name: String, value: Long): Expressions {
		define(name, BigDecimal.valueOf(value))

		return this
	}

	fun define(name: String, value: Double): Expressions {
		define(name, BigDecimal(value))

		return this
	}

	fun define(name: String, value: BigDecimal, override: Boolean = false): Expressions {
		evaluator.define0(name, value, override)

		return this
	}

	fun define(name: String, expression: String): Expressions {
		val expr = parse(expression)
		evaluator.define0(name, expr)

		return this
	}

	fun addFunction(name: String, func: (List<BigDecimal>) -> BigDecimal): Expressions {
		evaluator.addFunction(name, func)

		return this
	}

	fun eval(expression: String): BigDecimal {
		return evaluator.eval(parse(expression))
	}

	fun evalRound(expression: String): String {
		return evaluator.eval(parse(expression)).round(evaluator.context).stripTrailingZeros().toEngineeringString()
	}

	fun evalToString(expression: String): String {
		return try {
			eval(expression).round(evaluator.context).stripTrailingZeros().toEngineeringString()
		} catch (e: Throwable) {
			e.cause?.message ?: e.message ?: e.toString()
		}
	}

	private fun parse(expression: String): Expr {
		return parse(scan(expression))
	}

	private fun parse(tokens: List<Token>): Expr {
		return if (extended) ExtendedParser(tokens).parse() else DefaultParser(tokens).parse()
	}

	private fun scan(expression: String): List<Token> {
		return if (extended) ExtendedScanner(expression, evaluator.context).scanTokens() else DefaultScanner(
			expression,
			evaluator.context
		).scanTokens()
	}

	fun variables(): List<Pair<String, BigDecimal>> = evaluator.variables.let {
		val result: MutableList<Pair<String, BigDecimal>> = mutableListOf()
		it.forEach { (k, v) -> if (k !in ExtendedEvaluator.ConstantVariable) result.add(k to v) }
		result
	}

	fun setVariables(data: List<Pair<String, BigDecimal>>) {
		data.forEach { if (it.first !in ExtendedEvaluator.ConstantVariable) evaluator.variables[it.first] = it.second }
	}
}