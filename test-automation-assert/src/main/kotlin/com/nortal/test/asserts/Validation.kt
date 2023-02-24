/**
 * Copyright (c) 2022 Nortal AS
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
        title = builder.title ?: "",
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