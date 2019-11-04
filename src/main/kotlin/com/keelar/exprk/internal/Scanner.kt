package com.keelar.exprk.internal

import com.keelar.exprk.ExpressionException

internal interface Scanner {
	companion object {
		@JvmStatic fun invalidToken(c: Char) {
			throw ExpressionException("Invalid token '$c'")
		}

		private val digits = '0'..'9'
		private val Uppercase = 'A'..'Z'
		private val Lowercase = 'a'..'z'

		@JvmStatic fun isAlphaNumeric(c: Char) = c == '_' || c in digits || c in Uppercase || c in Lowercase
		@JvmStatic fun isDigits(c: Char) = c == '.' || c in digits

		@JvmStatic fun isCorrectBigDecimalSyntax(
			char: Char,
			previousChar: Char = '\u0000',
			nextChar: Char = '\u0000'
		): Boolean {
			return char in '0'..'9' || when (char) {
				'.'      -> previousChar != '.' && nextChar != '.'
				'e', 'E' -> previousChar.isDigit() && (nextChar.isDigit() || nextChar == '+' || nextChar == '-')
				'+', '-' -> (previousChar == 'e' || previousChar == 'E') && nextChar.isDigit()
				else     -> false
			}
		}
	}

	fun scanTokens(): List<Token>
}