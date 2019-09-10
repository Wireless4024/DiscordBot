package com.keelar.exprk

import ch.obermuhlner.math.big.BigDecimalMath
import com.keelar.exprk.internal.*
import com.keelar.exprk.internal.Function
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class ExpressionException(message: String) : RuntimeException(message)

@Suppress("unused")
class Expressions {

	private val evaluator = Evaluator()

	init {
		define("pi", BigDecimalMath.pi(evaluator.context))
		define("e", BigDecimalMath.e(evaluator.context))

		evaluator.addFunction("abs", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"abs requires one argument"
				)

				return arguments.first().abs()
			}
		})

		evaluator.addFunction("sum", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.isEmpty()) throw ExpressionException(
						"sum requires at least one argument"
				)

				return arguments.reduce { sum, bigDecimal ->
					sum.add(bigDecimal)
				}
			}
		})

		evaluator.addFunction("floor", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"abs requires one argument"
				)

				return arguments.first().setScale(0, RoundingMode.FLOOR)
			}
		})

		evaluator.addFunction("ceil", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"abs requires one argument"
				)

				return arguments.first().setScale(0, RoundingMode.CEILING)
			}
		})

		evaluator.addFunction("factorial", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"factorial requires one argument"
				)

				return BigDecimalMath.factorial(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("sqr", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"square requires one argument"
				)

				return arguments.first().multiply(arguments.first())
			}
		})

		evaluator.addFunction("cube", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"cube requires one argument"
				)

				return arguments.first().multiply(arguments.first()).multiply(arguments.first())
			}
		})

		evaluator.addFunction("sqrt", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"sqrt requires one argument"
				)

				return BigDecimalMath.sqrt(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("gamma", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"gamma requires one argument"
				)

				return BigDecimalMath.gamma(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("log", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"log requires one argument"
				)

				return BigDecimalMath.log(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("log2", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"log2 requires one argument"
				)

				return BigDecimalMath.log2(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("log10", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"log10 requires one argument"
				)

				return BigDecimalMath.log10(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("asin", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"asin requires one argument"
				)

				return BigDecimalMath.asin(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("asinh", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"asinh requires one argument"
				)

				return BigDecimalMath.asinh(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("sin", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"sin requires one argument"
				)

				return BigDecimalMath.sin(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("sinh", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"sinh requires one argument"
				)

				return BigDecimalMath.sinh(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("acos", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"acos requires one argument"
				)

				return BigDecimalMath.acos(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("acosh", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"acos requires one argument"
				)

				return BigDecimalMath.acosh(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("cos", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"cos requires one argument"
				)

				return BigDecimalMath.cos(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("cosh", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"cosh requires one argument"
				)

				return BigDecimalMath.cosh(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("atan", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"atan requires one argument"
				)

				return BigDecimalMath.atan(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("atanh", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"atanh requires one argument"
				)

				return BigDecimalMath.atanh(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("tan", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"tan requires one argument"
				)

				return BigDecimalMath.tan(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("tanh", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size != 1) throw ExpressionException(
						"tanh requires one argument"
				)

				return BigDecimalMath.tanh(arguments.first(), evaluator.context)
			}
		})

		evaluator.addFunction("root", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size < 2) throw ExpressionException(
						"root requires two argument"
				)

				return BigDecimalMath.root(arguments.first(), arguments[1], evaluator.context)
			}
		})

		evaluator.addFunction("round", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.size !in listOf(1, 2)) throw ExpressionException(
						"round requires either one or two arguments"
				)

				val value = arguments.first()
				val scale = if (arguments.size == 2) arguments.last().toInt() else 0

				return value.setScale(scale, roundingMode)
			}
		})

		evaluator.addFunction("min", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.isEmpty()) throw ExpressionException(
						"min requires at least one argument"
				)

				return arguments.min()!!
			}
		})

		evaluator.addFunction("max", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				if (arguments.isEmpty()) throw ExpressionException(
						"max requires at least one argument"
				)

				return arguments.max()!!
			}
		})

		evaluator.addFunction("if", object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal =
					if (arguments[0].signum() != 0) arguments[1] else arguments[2]
		})
	}

	val precision: Int
		get() = evaluator.context.precision

	val roundingMode: RoundingMode
		get() = evaluator.context.roundingMode

	fun setPrecision(precision: Int): Expressions {
		evaluator.context = MathContext(precision, roundingMode)


		return this
	}

	fun setRoundingMode(roundingMode: RoundingMode): Expressions {
		evaluator.context = MathContext(precision, roundingMode)

		return this
	}

	fun define(name: String, value: Long): Expressions {
		define(name, value.toString())

		return this
	}

	fun define(name: String, value: Double): Expressions {
		define(name, value.toString())

		return this
	}

	fun define(name: String, value: BigDecimal): Expressions {
		define(name, value.toPlainString())

		return this
	}

	fun define(name: String, expression: String): Expressions {
		val expr = parse(expression)
		evaluator.define(name, expr)

		return this
	}

	fun addFunction(name: String, function: Function): Expressions {
		evaluator.addFunction(name, function)

		return this
	}

	fun addFunction(name: String, func: (List<BigDecimal>) -> BigDecimal): Expressions {
		evaluator.addFunction(name, object : Function() {
			override fun call(arguments: List<BigDecimal>): BigDecimal {
				return func(arguments)
			}

		})

		return this
	}

	fun eval(expression: String): BigDecimal {
		return evaluator.eval(parse(expression))
	}

	fun evalToString(expression: String): String {
		return try {
			eval(expression).round(evaluator.context).stripTrailingZeros().toEngineeringString()
		} catch (e: Throwable) {
			e.cause?.message ?: e.message ?: ""
		}
	}

	private fun parse(expression: String): Expr {
		return parse(scan(expression))
	}

	private fun parse(tokens: List<Token>): Expr {
		return Parser(tokens).parse()
	}

	private fun scan(expression: String): List<Token> {
		return Scanner(expression, evaluator.context).scanTokens()
	}

}