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

import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Predicate
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL

object JsonPathUtils {

    @JvmStatic
    @Throws(IOException::class)
    fun <T> evaluate(json: Any?, jsonPath: String?, vararg predicates: Predicate?): T {
        return if (json is String) {
            JsonPath.read(json as String?, jsonPath, *predicates)
        } else if (json is ByteArray) {
            JsonPath.read(ByteArrayInputStream(json as ByteArray?), jsonPath, *predicates)
        } else if (json is File) {
            JsonPath.read(json as File?, jsonPath, *predicates)
        } else if (json is URL) {
            JsonPath.read(json.openStream(), jsonPath, *predicates)
        } else if (json is InputStream) {
            JsonPath.read(json as InputStream?, jsonPath, *predicates)
        } else {
            JsonPath.read(json, jsonPath, *predicates)
        }
    }
}