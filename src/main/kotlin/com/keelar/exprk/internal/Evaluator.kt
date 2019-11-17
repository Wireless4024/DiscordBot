package com.keelar.exprk.internal

import ch.obermuhlner.math.big.BigDecimalMath
import com.keelar.exprk.internal.DefaultEvaluator.Companion.ZERO
import java.math.BigDecimal
import java.math.MathContext

internal interface Evaluator : ExprVisitor<BigDecimal> {
	var context: MathContext

	val variables: HashMap<String, BigDecimal>

	fun eval(expr: Expr): BigDecimal

	fun define0(name: String, value: BigDecimal, override: Boolean)

	fun define0(name: String, value: BigDecimal)

	fun define0(name: String, value: Expr): Evaluator

	fun addFunction(
		names: Array<String>,
		function: (arguments: List<BigDecimal>) -> BigDecimal
	): ExprVisitor<BigDecimal>

	fun addFunction(
		name: String,
		function: (arguments: List<BigDecimal>) -> BigDecimal
	): ExprVisitor<BigDecimal>

	fun reset() {
		val pi = variables["pi"]
		val e = variables["e"]
		variables.clear()
		variables["pi"] = pi ?: BigDecimalMath.pi(context)
		variables["e"] = e ?: BigDecimalMath.e(context)
	}

	companion object {
		val EmptyFunction: (List<BigDecimal>) -> BigDecimal =
			{ args: List<BigDecimal> -> args.firstOrNull() ?: ZERO }
	}
}