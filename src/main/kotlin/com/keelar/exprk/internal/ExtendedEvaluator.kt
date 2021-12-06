package com.keelar.exprk.internal

import ch.obermuhlner.math.big.BigDecimalMath
import com.keelar.exprk.ExpressionException
import com.keelar.exprk.internal.DefaultEvaluator.Companion.BigDecimal
import com.keelar.exprk.internal.DefaultEvaluator.Companion.ExprAnd
import com.keelar.exprk.internal.DefaultEvaluator.Companion.ExprOr
import com.keelar.exprk.internal.DefaultEvaluator.Companion.IONE
import com.keelar.exprk.internal.DefaultEvaluator.Companion.IZERO
import com.keelar.exprk.internal.DefaultEvaluator.Companion.ZERO
import com.keelar.exprk.internal.TokenType.*
import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import java.math.RoundingMode.FLOOR
import java.util.*
import kotlin.collections.HashMap

internal class ExtendedEvaluator(scale: Int, roundingMode: RoundingMode) : Evaluator {

	companion object {

		@JvmField
		internal val ConstantVariable = arrayOf("pi", "e")
	}

	override var context: MathContext = MathContext(scale, roundingMode)

	override val variables: HashMap<String, BigDecimal> = hashMapOf()
	private val functions: MutableMap<String, (BigDecimalList) -> BigDecimalList> = mutableMapOf()

	override fun define0(name: String, value: BigDecimal, override: Boolean) {
		if (name in ConstantVariable && !override)
			throw UnsupportedOperationException("pi and e doesn't allow to override")
		variables += name to value
	}

	override fun define0(name: String, value: BigDecimal) = define0(name, value, false)

	override fun define0(name: String, expr: Expr): Evaluator {
		define0(name.lowercase(Locale.getDefault()), eval(expr))

		return this
	}

	override fun addFunction(
		names: Array<String>,
		function: (arguments: List<BigDecimal>) -> BigDecimal
	): ExprVisitor<BigDecimal> {
		names.forEach { addFunction(it, function) }
		return this
	}

	override fun addFunction(
		name: String,
		function: (arguments: List<BigDecimal>) -> BigDecimal
	): ExprVisitor<BigDecimal> {

		addFunctionL(name) { v: BigDecimalList -> BigDecimalList.of(function(v)) }

		return this
	}

	fun addFunctionL(
		name: String,
		function: (arguments: BigDecimalList) -> BigDecimalList
	): ExprVisitor<BigDecimal> {
		if (functions.containsKey(name.lowercase(Locale.getDefault())))
			throw UnsupportedOperationException("function $name already existed")

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
			PLUS -> left + right
			MINUS -> left - right
			STAR -> left * right
			SLASH -> left.divide(right, context)
			DOUBLE_SLASH -> left.divide(right, context).setScale(0, FLOOR)
			MODULO -> left.remainder(right, context)
			EXPONENT -> left pow right
			EQUAL_EQUAL -> BigDecimal(left == right)
			NOT_EQUAL -> BigDecimal(left != right)
			GREATER -> BigDecimal(left > right)
			GREATER_EQUAL -> BigDecimal(left >= right)
			LESS -> BigDecimal(left < right)
			LESS_EQUAL -> BigDecimal(left <= right)

			// bitwise operator
			SHIFT_LEFT -> left shl right
			SHIFT_RIGHT -> left shr right
			AND -> left and right
			OR -> left or right
			XOR -> left xor right
			NAND -> left nand right
			NOR -> left nor right
			NXOR -> left nxor right
			ROL -> left rol right

			else          -> throw ExpressionException(
				"Invalid binary operator '${expr.operator.lexeme}'"
			)
		}
	}

	private infix fun BigDecimal.shl(right: BigDecimal): BigDecimal {
		return BigDecimal(this.unscaledValue().shiftLeft(right.toInt()), this.scale())
	}

	private infix fun BigDecimal.shr(right: BigDecimal): BigDecimal {
		return this.divide(BigDecimal(IONE.shiftLeft(right.toInt()), context))
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
		return BigDecimal(this.toBigInteger().and(right.toBigInteger()).not())
	}

	private infix fun BigDecimal.nor(right: BigDecimal): BigDecimal {
		return BigDecimal(this.toBigInteger().or(right.toBigInteger()).not())
	}

	private infix fun BigDecimal.nxor(right: BigDecimal): BigDecimal {
		return BigDecimal(this.toBigInteger().xor(right.toBigInteger()).not())
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
		val mask = IZERO.setBit(bitSize).subtract(IONE)
		return value.shiftLeft(shift).or(topBits).and(mask)
	}

	private fun BigInteger.ensureBit(bits: Int): BigInteger {
		return this.and(IONE.shiftLeft(bits).not().negate())
	}

	override fun visitUnaryExpr(expr: UnaryExpr): BigDecimal {
		val right = eval(expr.right)

		return when (expr.operator.type) {
			MINUS -> right.negate()
			NOT -> BigDecimal(right.toBigInteger().not())
			else  -> throw ExpressionException("Invalid unary operator")
		}
	}

	override fun visitCallExpr(expr: CallExpr): BigDecimal {
		val name = expr.name
		val function: (BigDecimalList) -> BigDecimalList =
			functions[name.lowercase(Locale.getDefault())] ?: Evaluator.EmptyFunctionList

		return function(BigDecimalList.of(expr.arguments.map { eval(it) })).sumOf { it }
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

	private infix fun BigDecimal.pow(n: BigDecimal): BigDecimal {
		return BigDecimalMath.pow(this, n, context)
	}

}