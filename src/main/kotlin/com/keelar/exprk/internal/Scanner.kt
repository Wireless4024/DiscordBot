package com.keelar.exprk.internal

import ch.obermuhlner.math.big.BigDecimalMath
import com.keelar.exprk.ExpressionException
import com.keelar.exprk.internal.TokenType.*
import java.math.MathContext

private fun invalidToken(c: Char) {
	throw ExpressionException("Invalid token '$c'")
}

internal class Scanner(
		private val source: String,
		private val mathContext: MathContext
) {

	private val tokens: MutableList<Token> = mutableListOf()
	private var start = 0
	private var current = 0

	fun scanTokens(): List<Token> {
		while (!isAtEnd()) scanToken()

		tokens.add(Token(EOF, "", null))
		return tokens
	}

	private fun isAtEnd(): Boolean {
		return current >= source.length
	}

	private fun scanToken() {
		start = current
		val c = advance()

		when (c.toLowerCase()) {
			' ',
			'\r',
			'\t' -> {
				// Ignore whitespace.
			}
			'+'  -> addToken(PLUS)
			'-'  -> addToken(MINUS)
			'*'  -> if (match('*')) addToken(EXPONENT, "**") else addToken(STAR)
			'/'  -> if (match('/')) addToken(DOUBLE_SLASH, "//") else addToken(SLASH)
			'%'  -> addToken(MODULO)
			'^'  -> addToken(XOR, "^")
			'r'  -> if (match('o'))
				when {
					match('r') -> addToken(ROR, "ror")
					match('l') -> addToken(ROL, "rol")
					else       -> invalidToken(c)
				}
			else invalidToken(c)
			'~'  -> when {
				match('&') -> addToken(NAND, "~&")
				match('|') -> addToken(NOR, "~|")
				match('^') -> addToken(NXOR, "~^")
				else       -> addToken(NOT)
			}
			'='  -> if (match('=')) addToken(EQUAL_EQUAL) else addToken(ASSIGN)
			'!'  -> if (match('=')) addToken(NOT_EQUAL) else invalidToken(c)
			'>'  -> when {
				match('=') -> addToken(GREATER_EQUAL)
				match('>') -> addToken(SHIFT_RIGHT, ">>")
				else       -> addToken(GREATER)
			}
			'<'  -> when {
				match('=') -> addToken(LESS_EQUAL)
				match('<') -> addToken(SHIFT_LEFT, "<<")
				else       -> addToken(LESS)
			}
			'|'  -> if (match('|')) addToken(BAR_BAR) else addToken(OR, "|")
			'&'  -> if (match('&')) addToken(AMP_AMP) else addToken(AND, "&")
			','  -> addToken(COMMA)
			'('  -> addToken(LEFT_PAREN)
			')'  -> addToken(RIGHT_PAREN)
			else -> {
				when {
					c.isDigit() -> number()
					c.isAlpha() -> identifier()
					else        -> invalidToken(c)
				}
			}
		}
	}

	private fun isCorrectBigDecimalSyntax(char: Char,
	                                      previousChar: Char = '\u0000',
	                                      nextChar: Char = '\u0000'): Boolean {
		return char.isDigit() || when (char) {
			'.'      -> true
			'e'      -> previousChar.isDigit() && (nextChar.isDigit() || nextChar == '+' || nextChar == '-')
			'+', '-' -> previousChar == 'e' && nextChar.isDigit()
			else     -> false
		}
	}

	private fun number() {
		while (peek().isDigit()) advance()

		if (isCorrectBigDecimalSyntax(peek(), peekPrevious(), peekNext())) {
			advance()
			while (isCorrectBigDecimalSyntax(peek(), peekPrevious(), peekNext())) advance()
		}

		val value = BigDecimalMath.toBigDecimal(source.substring(start, current))

		addToken(NUMBER, value)
	}

	private fun identifier() {
		while (peek().isAlphaNumeric()) advance()

		addToken(IDENTIFIER)
	}

	private fun advance() = source[current++]

	private fun peekPrevious(): Char = if (current > 0) source[current - 1] else '\u0000'

	private fun peek(): Char {
		return if (isAtEnd()) {
			'\u0000'
		} else {
			source[current]
		}
	}

	private fun peekNext(): Char {
		return if (current + 1 >= source.length) {
			'\u0000'
		} else {
			source[current + 1]
		}
	}

	private fun match(expected: Char): Boolean {
		if (isAtEnd()) return false
		if (source[current] != expected) return false

		current++
		return true
	}

	private fun addToken(type: TokenType) = addToken(type, null)

	private fun addToken(type: TokenType, literal: Any?) {
		val text = source.substring(start, current)
		tokens.add(Token(type, text, literal))
	}

	private fun Char.isAlphaNumeric() = isAlpha() || isDigit()

	private fun Char.isAlpha() = this in 'a'..'z'
	                             || this in 'A'..'Z'
	                             || this == '_'

	private fun Char.isDigit() = this == '.' || this in '0'..'9'

}