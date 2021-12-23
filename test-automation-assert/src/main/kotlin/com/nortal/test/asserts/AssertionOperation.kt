package com.nortal.test.asserts

/**
 * Defines available assertion operations
 */
enum class AssertionOperation {
    /**
     * Operation where value returned by applying expression is checked to be equal to the expectedValue
     * expectedValue.equals(actualValue)
     */
    EQUALS,

    /**
     * Operation where value returned by applying expressiong is checked to be not equal to the the expactedValue
     * !expectedValue.equals(actualValue)
     */
    NOT_EQUALS,

    /**
     * Operation where value returned by applying expression is expected to be null
     * actualValue == null
     */
    NULL,

    /**
     * Operation where value returned by applying expression is expected to be not null
     * actualValue != null
     */
    NOT_NULL,

    /**
     * Operation where value returned by applying expression is expected to be a String and to be contained in the expected value.
     * expectedValue.contains(actualValue) == true
     */
    CONTAINS,

    /**
     * Operation where value returned by applying expression is expected to be a List and to be contained in the expected value.
     * expectedValue.contains(actualValue) == true
     */
    LIST_CONTAINS,

    /**
     * Operation where value returned by applying expression is expected to be an object and to be contained in expected value which
     * is expected to be a collection.
     * expectedValues.contains(actualValue) == true
     */
    LIST_CONTAINS_VALUE,

    /**
     * Operation where value returned by applying expression is expected to be a List and to be contained in the expected value.
     * expectedValue.contains(actualValue) == false
     */
    LIST_EXCLUDES,

    /**
     * Operation where value returned by applying expression is expected to be a List and to be contained in the expected value.
     * expectedValue.containsAll(actualValue) && actualValue.containsAll(expectedValue) == true
     */
    LIST_EQUALS,

    /**
     * Operation where value returned by applying expression is expected to be a List which should be empty.
     * actualValue.isEmpty() == true
     */
    EMPTY,

    /**
     * Operation where assertion is performed by evaluating the expression.
     * In such case expression has to return a boolean
     */
    EXPRESSION
}