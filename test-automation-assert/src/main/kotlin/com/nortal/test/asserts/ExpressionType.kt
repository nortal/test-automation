package com.nortal.test.asserts

/**
 * Defines the type of the expression
 */
enum class ExpressionType {
    /**
     * Expression that is evaluated against the base expression
     */
    RELATIVE,

    /**
     * Expression that is evaluated against the root of the context
     */
    ABSOLUTE
}