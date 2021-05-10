package com.nortal.test.core.assertions;

import java.text.MessageFormat;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * This class represents an assertion that will be carried out.
 */
@Data
@Builder
public class Assertion {
	/**
	 * Assertion messages describing what the assertion does.
	 * e.g. Verify ratePlanSoc or Verify correct line count
	 */
	@Builder.Default
	private String message = "";

	/**
	 * SpEL expression that retrieves actual value from the context.
	 * Expectation here is that once the expression is parsed and getValue is executed on the context whatever actualValue is returned will
	 * pass the selected operation against the expectedValue.
	 *
	 * Some default variables can be used in the expression:
	 * #root to reference the root context object
	 * #expected to reference the expectedValue
	 */
	@Builder.Default
	private String expression = "";

	@Builder.Default
	private ExpressionType expressionType = ExpressionType.RELATIVE;

	/**
	 * Operation that will be executed using expected and actual values as operands
	 */
	@Builder.Default
	private AssertionOperation operation = AssertionOperation.EQUALS;

	/**
	 * Expected value.
	 * Depending on the selected operation may be optional (e.g. for NOT_NULL)
	 */
	private Object expectedValue;

	/**
	 * Context values.
	 * Designed to be used in tandem with EXPRESSION
	 */
	private Map<String, Object> contextValues;

	/**
	 * Some assertions can act as gatekeepers, meaning that if they fail, there is little meaning in running the rest of them.
	 * If TRUE will skip the rest of assertions.
	 * FALSE by default
	 */
	@Builder.Default
	private Boolean skipRestIfFailed = Boolean.FALSE;

	/**
	 * Some assertions are just simple assertions where you would want to compare two numbers and check if they match
	 * and for them to show up in the report. For this actual value can be set.
	 */
	private Object actualValue;

	/**
	 * Defines the type of the expression
	 */
	public enum ExpressionType {
		/**
		 * Expression that is evaluated against the base expression
		 */
		RELATIVE,
		/**
		 * Expression that is evaluated against the root of the context
		 */
		ABSOLUTE
	}

	/**
	 * Defines available assertion operations
	 */
	public enum AssertionOperation {
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

}

