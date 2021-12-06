package com.keelar.exprk.internal

import ch.obermuhlner.math.big.BigDecimalMath
import com.keelar.exprk.ExpressionException
import com.keelar.exprk.internal.TokenType.*
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.util.*
import kotlin.collections.HashMap

internal class DefaultEvaluator(scale: Int, roundingMode: RoundingMode) : Evaluator {

	override var context: MathContext = MathContext(scale, roundingMode)

	override val variables: HashMap<String, BigDecimal> = hashMapOf()
	private val functions: MutableMap<String, (List<BigDecimal>) -> BigDecimal> = mutableMapOf()

	override fun define0(name: String, value: BigDecimal, override: Boolean) {
		if (name in ExtendedEvaluator.ConstantVariable && !override)
			throw UnsupportedOperationException("pi and e doesn't allow to override")
		variables += name to value
	}

	override fun define0(name: String, value: BigDecimal) = define0(name, value, false)

	override fun define0(name: String, value: Expr): Evaluator {
        define0(name.lowercase(Locale.getDefault()), eval(value))

		return this
	}

	override fun addFunction(
		names: Array<String>,
		function: (arguments: List<BigDecimal>) -> BigDecimal
	): ExprVisitor<BigDecimal> {
		names.forEach {
			addFunction(it, function)
		}

		return this
	}

	override fun addFunction(name: String, function: (List<BigDecimal>) -> BigDecimal): ExprVisitor<BigDecimal> {
        functions += name.lowercase(Locale.getDefault()) to function

        return this
    }

	override fun eval(expr: Expr): BigDecimal {
		return expr.accept(this)
	}

	override fun visitAssignExpr(expr: AssignExpr): BigDecimal {
		val value = eval(expr.value)

		define0(expr.name.lexeme, value)

		return value
	}

	override fun visitLogicalExpr(expr: LogicalExpr): BigDecimal {
		val left = expr.left
		val right = expr.right

		return when (expr.operator.type) {
			BAR_BAR -> ExprOr(this, left, right)
			AMP_AMP -> ExprAnd(this, left, right)
			else    -> throw ExpressionException(
				"Invalid logical operator '${expr.operator.lexeme}'"
			)
		}
	}

	override fun visitBinaryExpr(expr: BinaryExpr): BigDecimal {
		val left = eval(expr.left)
		val right = eval(expr.right)

		return when (expr.operator.type) {
			PLUS          -> left + right
			MINUS         -> left - right
			STAR          -> left * right
			SLASH         -> left.divide(right, context)
			MODULO        -> left.remainder(right, context)
			EXPONENT      -> BigDecimalMath.pow(left, right, context)
			EQUAL_EQUAL   -> BigDecimal(left == right)
			NOT_EQUAL     -> BigDecimal(left != right)
			GREATER       -> BigDecimal(left > right)
			GREATER_EQUAL -> BigDecimal(left >= right)
			LESS          -> BigDecimal(left < right)
			LESS_EQUAL    -> BigDecimal(left <= right)
			else          -> throw ExpressionException(
				"Invalid binary operator '${expr.operator.lexeme}'"
			)
		}
	}

	override fun visitUnaryExpr(expr: UnaryExpr): BigDecimal {
		val right = eval(expr.right)

		return when (expr.operator.type) {
			MINUS -> {
				right.negate()
			}
			else  -> throw ExpressionException("Invalid unary operator")
		}
	}

	override fun visitCallExpr(expr: CallExpr): BigDecimal {
		val name = expr.name
		val function = functions[name.lowercase(Locale.getDefault())] ?: Evaluator.EmptyFunction

		return function(expr.arguments.map { eval(it) })
	}

	override fun visitLiteralExpr(expr: LiteralExpr): BigDecimal {
		return expr.value
	}

	override fun visitVariableExpr(expr: VariableExpr): BigDecimal {
		val name = expr.name.lexeme

		return variables[name.lowercase(Locale.getDefault())] ?: ZERO
	}

	override fun visitGroupingExpr(expr: GroupingExpr): BigDecimal {
		return eval(expr.expression)
	}

	companion object {

		/* NOT NULLABLE constants kotlin will check this at single time */
		@JvmField val ZERO: BigDecimal = BigDecimal.ZERO
		@JvmField val ONE: BigDecimal = BigDecimal.ONE
		@JvmField val IZERO: BigInteger = BigInteger.ZERO
		@JvmField val IONE: BigInteger = BigInteger.ONE

		@Suppress("NOTHING_TO_INLINE")
		inline fun BigDecimal(boolean: Boolean) = if (boolean) ONE else ZERO

		@JvmStatic
		fun ExprOr(self: Evaluator, left: Expr, right: Expr): BigDecimal {
			val left = self.eval(left)

			// short-circuit if left is truthy
			if (left.signum() != 0) return ONE

			return BigDecimal(0 != self.eval(right).signum())
		}

		fun ExprAnd(self: Evaluator, left: Expr, right: Expr): BigDecimal {
			val left = self.eval(left)

			// short-circuit if left is falsey
			if (left.signum() == 0) return ZERO

			return BigDecimal(0 != self.eval(right).signum())
		}
	}
}