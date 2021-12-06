package com.keelar.exprk.internal

import java.math.BigDecimal

class BigDecimalList : ArrayList<BigDecimal>() {
    companion object {
        fun of(args: Collection<BigDecimal>): BigDecimalList {
            return BigDecimalList().apply { addAll(args) }
        }

        fun of(arg: BigDecimal): BigDecimalList {
            return BigDecimalList().apply { add(arg) }
        }
    }
}