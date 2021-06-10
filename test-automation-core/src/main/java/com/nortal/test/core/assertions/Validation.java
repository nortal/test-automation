package com.nortal.test.core.assertions;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

/**
 * This class represents a validation of some sort.
 * It can contain multiple assertions that all act against the same context.
 * If any of the assertions fail, the whole validation is considered to have not passed.
 */
@Builder
@Data
public class Validation {

	/**
	 * Title of the validation.
	 * Will be visible in the report as the name of the attachment
	 */
	private final String title;

	/**
	 * The context object that assertions will be executed against.
	 * Will be visible in the report as an attachment (in JSON format)
	 */
	private final Object context;

	/**
	 * Description of what the context object is.
	 * Will be visible in the report as the name of the context attachment.
	 */
	private final String contextDescription;

	/**
	 * Base expression to resolve the actual value.
	 * This expression will be used as a prefix for every assertion that has expression type RELATIVE
	 */
	@Builder.Default
	private String baseExpression = "";

	/**
	 * Assertions that will be executed against the context object.
	 */
	@Singular
	private List<Assertion> assertions;
}