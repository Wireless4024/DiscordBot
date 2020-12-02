package com.keelar.exprk.internal

import ch.obermuhlner.math.big.BigDecimalMath
import com.keelar.exprk.internal.Scanner.Companion.isAlphaNumeric
import com.keelar.exprk.internal.Scanner.Companion.isCorrectBigDecimalSyntax
import com.keelar.exprk.internal.TokenType.*
import java.math.BigDecimal
import java.math.MathContext

internal class ExtendedScanner(
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
			'\n',
			'\t' -> {
				// Ignore whitespace.
			}
			'+' -> addToken(PLUS)
			'-' -> addToken(MINUS)
			'*' ->
				if (match('*'))
					if (match('*'))
						if (match('*')) {
							var counts = 4
							while (match('*')) counts += 1
							addToken(`STARRRRRR!`, "*".repeat(counts))
						} else addToken(CUBE, "***")
					else {
						if (match('[')) {
							addToken(LIST_UNPACK)
							addToken(LIST_START)
						} else
							addToken(EXPONENT, "**")
					}
				else addToken(STAR)
			'/' -> if (match('/')) addToken(DOUBLE_SLASH, "//") else addToken(SLASH)
			'%' -> addToken(MODULO)
			'^' -> addToken(XOR, "^")
			/*'r'  -> if (match('o'))
				when {
					match('r') -> addToken(ROR, "ror")
					match('l') -> addToken(ROL, "rol")
					else       -> invalidToken(c)
				}
			else invalidToken(c)
			*/
			'~' -> when {
				match('&') -> addToken(NAND, "~&")
				match('|') -> addToken(NOR, "~|")
				match('^') -> addToken(NXOR, "~^")
				else       -> addToken(NOT)
			}
			'=' -> if (match('=')) addToken(EQUAL_EQUAL) else addToken(ASSIGN)
			'!' -> if (match('=')) addToken(NOT_EQUAL) else Scanner.invalidToken(c)
			'>' -> when {
				match('=') -> addToken(GREATER_EQUAL)
				match('>') -> addToken(SHIFT_RIGHT, ">>")
				else       -> addToken(GREATER)
			}
			'<' -> when {
				match('=') -> addToken(LESS_EQUAL)
				match('<') -> addToken(SHIFT_LEFT, "<<")
				else       -> addToken(LESS)
			}
			'|' -> if (match('|')) addToken(BAR_BAR) else addToken(OR, "|")
			'&' -> if (match('&')) addToken(AMP_AMP) else addToken(AND, "&")
			',' -> addToken(COMMA)
			'(' -> addToken(LEFT_PAREN)
			')' -> addToken(RIGHT_PAREN)
			'[' -> addToken(LIST_START)
			']' -> addToken(LIST_END)
			else -> {
				when {
					c.isDigit()       -> number()
					isAlphaNumeric(c) -> identifier()
					else              -> Scanner.invalidToken(c)
				}
			}
		}
	}

	private fun number() {
		while (isCorrectBigDecimalSyntax(peek(), peekPrevious(), peekNext())) advance()
		addToken(NUMBER, postFix(peek(), peekNext()) ?: BigDecimalMath.toBigDecimal(source.substring(start, current)))
	}

	private fun postFix(now: Char, next: Char): BigDecimal? {
		return when {
			now == '!' && next != '=' -> BigDecimalMath.factorial(
				BigDecimalMath.toBigDecimal(source.substring(start, current)), mathContext
			).also { advance() }
			else                      -> null
		}
	}

	private fun identifier() {
		while (isAlphaNumeric(peek())) advance()

		addToken(IDENTIFIER)
	}

	private fun advance() = source[current++]

	private fun peekPrevious(): Char = if (current > 0) source[current - 1] else '\u0000'

	private fun peek() = if (isAtEnd()) '\u0000' else source[current]

	private fun peekNext() = if (current + 1 >= source.length) '\u0000' else source[current + 1]

	private fun match(expected: Char): Boolean {
		if (isAtEnd() || source[current] != expected) return false
		++current
		return true
	}

	private fun addToken(type: TokenType) = addToken(type, null)

	private fun addToken(type: TokenType, literal: Any?) =
		tokens.add(Token(type, source.substring(start, current), literal))
}