package com.keelar.exprk.internal

import com.keelar.exprk.internal.Scanner.Companion.invalidToken
import com.keelar.exprk.internal.Scanner.Companion.isAlphaNumeric
import com.keelar.exprk.internal.Scanner.Companion.isCorrectBigDecimalSyntax
import com.keelar.exprk.internal.TokenType.*
import java.math.MathContext

internal class DefaultScanner(
	private val source: String,
	private val mathContext: MathContext
) : Scanner {

	private val tokens: MutableList<Token> = mutableListOf()
	private var start = 0
	private var current = 0

	override fun scanTokens(): List<Token> {
		while (!isAtEnd()) {
			scanToken()
		}

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
			'*'  -> addToken(STAR)
			'/'  -> addToken(SLASH)
			'%'  -> addToken(MODULO)
			'^'  -> addToken(EXPONENT)
			'='  -> if (match('=')) addToken(EQUAL_EQUAL) else addToken(ASSIGN)
			'!'  -> if (match('=')) addToken(NOT_EQUAL) else invalidToken(c)
			'>'  -> if (match('=')) addToken(GREATER_EQUAL) else addToken(GREATER)
			'<'  -> if (match('=')) addToken(LESS_EQUAL) else addToken(LESS)
			'|'  -> if (match('|')) addToken(BAR_BAR) else invalidToken(c)
			'&'  -> if (match('&')) addToken(AMP_AMP) else invalidToken(c)
			','  -> addToken(COMMA)
			'('  -> addToken(LEFT_PAREN)
			')'  -> addToken(RIGHT_PAREN)
			else -> {
				when {
					c.isDigit()       -> number()
					isAlphaNumeric(c) -> identifier()
					else              -> invalidToken(c)
				}
			}
		}
	}

	private fun number() {
		while (peek().isDigit()) advance()

		if (isCorrectBigDecimalSyntax(peek(), peekPrevious(), peekNext())) {
			advance()
			while (isCorrectBigDecimalSyntax(peek(), peekPrevious(), peekNext())) advance()
		}

		val value = source
			.substring(start, current)
			.toBigDecimal(mathContext)

		addToken(NUMBER, value)
	}

	private fun identifier() {
		while (isAlphaNumeric(peek())) advance()

		addToken(IDENTIFIER)
	}

	private fun advance() = source[current++]

	private fun peek(): Char {
		return if (isAtEnd()) {
			'\u0000'
		} else {
			source[current]
		}
	}

	private fun peekPrevious(): Char = if (current > 0) source[current - 1] else '\u0000'

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
}