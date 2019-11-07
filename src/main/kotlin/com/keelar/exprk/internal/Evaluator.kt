package com.keelar.exprk.internal

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

	companion object {
		val EmptyFunction: (List<BigDecimal>) -> BigDecimal =
			{ args: List<BigDecimal> -> args.firstOrNull() ?: BigDecimal.ZERO }
	}
}