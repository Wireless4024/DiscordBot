package com.keelar.exprk.internal

import ch.obermuhlner.math.big.BigDecimalMath
import com.keelar.exprk.ExpressionException
import com.keelar.exprk.internal.TokenType.*
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode.FLOOR

internal class Evaluator : ExprVisitor<BigDecimal> {
	internal var context: MathContext = MathContext(128, FLOOR)

	private val variables: LinkedHashMap<String, BigDecimal> = linkedMapOf()
	private val functions: MutableMap<String, Function> = mutableMapOf()

	internal fun define0(name: String, value: BigDecimal, override: Boolean = false) {
		if (name in arrayOf("pi", "e") && !override)
			throw UnsupportedOperationException("pi and e doesn't allow to override")
		variables += name to value
	}

	fun define0(name: String, expr: Expr): Evaluator {
		define0(name.toLowerCase(), eval(expr))

		return this
	}

	fun addFunction(name: String, function: Function): Evaluator {
		if (functions.containsKey(name.toLowerCase()))
			throw UnsupportedOperationException("function $name already existed")
		functions += name.toLowerCase() to function

		return this
	}

	fun eval(expr: Expr): BigDecimal {
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
			BAR_BAR -> left or right
			AMP_AMP -> left and right
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
			DOUBLE_SLASH  -> left.divide(right, context).setScale(0, FLOOR)
			MODULO        -> left.remainder(right, context)
			EXPONENT      -> left pow right
			EQUAL_EQUAL   -> (left == right).toBigDecimal()
			NOT_EQUAL     -> (left != right).toBigDecimal()
			GREATER       -> (left > right).toBigDecimal()
			GREATER_EQUAL -> (left >= right).toBigDecimal()
			LESS          -> (left < right).toBigDecimal()
			LESS_EQUAL    -> (left <= right).toBigDecimal()

			// new operator
			SHIFT_LEFT    -> left shl right
			SHIFT_RIGHT   -> left shr right
			AND           -> left and right
			OR            -> left or right
			XOR           -> left xor right
			NAND          -> left nand right
			NOR           -> left nor right
			NXOR          -> left nxor right
			ROL           -> left rol right

			else          -> throw ExpressionException(
					"Invalid binary operator '${expr.operator.lexeme}'"
			)
		}
	}

	private infix fun BigDecimal.shl(right: BigDecimal): BigDecimal {
		return BigDecimal(this.unscaledValue().shiftLeft(right.toInt()), this.scale())
	}

	private infix fun BigDecimal.shr(right: BigDecimal): BigDecimal {
		return this.divide(BigDecimal(BigInteger.ONE.shiftLeft(right.toInt()), context))
	}

	private infix fun BigDecimal.and(right: BigDecimal): BigDecimal {
		return BigDecimal(this.toBigInteger().and(right.toBigInteger()))
	}

	private infix fun BigDecimal.or(right: BigDecimal): BigDecimal {
		return BigDecimal(this.toBigInteger().or(right.toBigInteger()))
	}

	private infix fun BigDecimal.xor(right: BigDecimal): BigDecimal {
		return BigDecimal(this.toBigInteger().xor(right.toBigInteger()))
	}

	private infix fun BigDecimal.nand(right: BigDecimal): BigDecimal {
		return BigDecimal(this.toBigInteger().xor(right.toBigInteger()))
	}

	private infix fun BigDecimal.nor(right: BigDecimal): BigDecimal {
		return BigDecimal(this.toBigInteger().xor(right.toBigInteger()))
	}

	private infix fun BigDecimal.nxor(right: BigDecimal): BigDecimal {
		return BigDecimal(this.toBigInteger().xor(right.toBigInteger()))
	}

	private infix fun BigDecimal.rol(bright: BigDecimal): BigDecimal {
		val left = this.toBigInteger()
		val length = left.bitLength()
		val right = bright.toBigInteger().mod(BigInteger.valueOf(length.toLong()))
		return BigDecimal(rotateLeft(left, right.toInt()))
	}

	fun rotateLeft(value: BigInteger, shift: Int): BigInteger? {
		val bitSize = value.bitLength()
		val topBits = value.shiftRight(bitSize - shift)
		val mask = BigInteger.ZERO.setBit(bitSize).subtract(BigInteger.ONE)
		return value.shiftLeft(shift).or(topBits).and(mask)
	}

	private fun BigInteger.ensureBit(bits: Int): BigInteger {
		return this.and(BigInteger.ONE.shiftLeft(bits).not().negate())
	}

	override fun visitUnaryExpr(expr: UnaryExpr): BigDecimal {
		val right = eval(expr.right)

		return when (expr.operator.type) {
			MINUS -> right.negate()
			NOT   -> BigDecimal(right.toBigInteger().not())
			else  -> throw ExpressionException("Invalid unary operator")
		}
	}

	override fun visitCallExpr(expr: CallExpr): BigDecimal {
		val name = expr.name
		val function = functions[name.toLowerCase()] ?: throw ExpressionException("Undefined function '$name'")

		return function.call(expr.arguments.map { eval(it) })
	}

	override fun visitLiteralExpr(expr: LiteralExpr): BigDecimal {
		return expr.value
	}

	override fun visitVariableExpr(expr: VariableExpr): BigDecimal {
		val name = expr.name.lexeme

		return variables[name.toLowerCase()] ?: throw ExpressionException("Undefined variable '$name'")
	}

	override fun visitGroupingExpr(expr: GroupingExpr): BigDecimal {
		return eval(expr.expression)
	}

	private infix fun Expr.or(right: Expr): BigDecimal {
		val left = eval(this)

		// short-circuit if left is truthy
		if (left.isTruthy()) return BigDecimal.ONE

		return eval(right).isTruthy().toBigDecimal()
	}

	private infix fun Expr.and(right: Expr): BigDecimal {
		val left = eval(this)

		// short-circuit if left is falsey
		if (!left.isTruthy()) return BigDecimal.ZERO

		return eval(right).isTruthy().toBigDecimal()
	}

	private fun BigDecimal.isTruthy(): Boolean {
		return this.signum() != 0
	}

	private fun Boolean.toBigDecimal(): BigDecimal {
		return if (this) BigDecimal.ONE else BigDecimal.ZERO
	}

	private infix fun BigDecimal.pow(n: BigDecimal): BigDecimal {
		return BigDecimalMath.pow(this, n, context)
	}

}