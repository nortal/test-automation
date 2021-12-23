package com.nortal.test.asserts

/**
 * This class represents a validation of some sort.
 * It can contain multiple assertions that all act against the same context.
 * If any of the assertions fail, the whole validation is considered to have not passed.
 */
data class Validation(
    /**
     * Title of the validation.
     * Will be visible in the report as the name of the attachment
     */
    val title: String,
    /**
     * The context object that assertions will be executed against.
     * Will be visible in the report as an attachment (in JSON format)
     */
    val context: Any,

    /**
     * Assertions that will be executed against the context object.
     */
    val assertions: List<Assertion> = ArrayList(),

    /**
     * Description of what the context object is.
     * Will be visible in the report as the name of the context attachment.
     */
    val contextDescription: String? = null,

    /**
     * Base expression to resolve the actual value.
     * This expression will be used as a prefix for every assertion that has expression type RELATIVE
     */
    val baseExpression: String = ""
) {

    private constructor(builder: Builder) : this(
        title = builder.title ?: throw NullPointerException(),
        context = builder.context ?: throw NullPointerException(),
        assertions = builder.assertions,
        contextDescription = builder.contextDescription,
        baseExpression = builder.baseExpression ?: ""

    )

    class Builder {
        var title: String? = null
            private set
        var context: Any? = null
            private set
        var assertions: MutableList<Assertion> = ArrayList()
            private set
        var contextDescription: String? = null
            private set
        var baseExpression: String? = null
            private set

        fun title(title: String) = apply { this.title = title }

        fun context(context: Any?) = apply { this.context = context }

        fun assertions(assertions: MutableList<Assertion>) = apply { this.assertions = assertions }

        fun assertion(assertion: Assertion) = apply { this.assertions.add(assertion) }

        fun contextDescription(contextDescription: String) = apply { this.contextDescription = contextDescription }

        fun baseExpression(baseExpression: String) = apply { this.baseExpression = baseExpression }

        fun build() = Validation(this)
    }


}