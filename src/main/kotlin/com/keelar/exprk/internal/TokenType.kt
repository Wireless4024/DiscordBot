package com.keelar.exprk.internal

internal enum class TokenType {

	// Basic operators
	PLUS,
	MINUS,
	STAR,
	`STARRRRRR!`,
	SLASH,
	DOUBLE_SLASH, // floor divide
	MODULO,
	EXPONENT,
	CUBE,
	ASSIGN,

	// binary operators
	SHIFT_LEFT,
	SHIFT_RIGHT,
	AND,
	OR,
	XOR,
	NOT,
	NAND,
	NOR,
	NXOR,
	ROR, // rotate right
	ROL, // rotate left

	// Logical operators
	EQUAL_EQUAL,
	NOT_EQUAL,
	GREATER,
	GREATER_EQUAL,
	LESS,
	LESS_EQUAL,
	BAR_BAR,
	AMP_AMP,

	// Other
	COMMA,

	// Parentheses
	LEFT_PAREN,
	RIGHT_PAREN,

	// List support
	LIST_UNPACK,
	LIST_START,
	LIST_END,

	// Literals
	NUMBER,
	IDENTIFIER,

	EOF

}