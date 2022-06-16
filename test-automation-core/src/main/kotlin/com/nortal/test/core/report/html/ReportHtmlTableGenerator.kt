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
package com.nortal.test.core.report.html

import com.google.common.collect.ImmutableList
import com.nortal.test.core.file.ClasspathFileResolver
import org.springframework.stereotype.Component
import java.lang.StringBuilder
import java.util.function.Consumer

@Component
class ReportHtmlTableGenerator(
    private val classpathFileResolver: ClasspathFileResolver
) {
    fun generateTable(table: List<List<String?>>, appendRowNums: Boolean): String {
        val html = StringBuilder("<table class=\"table table-striped table-fit-content\">")
        populateHeader(html, table[0], appendRowNums)
        populateBody(html, table, appendRowNums)
        html.append("</table>")
        html.append(getTableCss())
        return html.toString()
    }

    private fun populateHeader(html: StringBuilder, headerValues: List<String?>, appendRowNums: Boolean) {
        val finalHeaderValues: List<String?>
        finalHeaderValues = if (appendRowNums) {
            ImmutableList.Builder<String?>().addAll(headerValues).add("#").build()
        } else {
            headerValues
        }
        html.append("<thead><tr>")
        finalHeaderValues.forEach(Consumer { value: String? -> html.append("<th>").append(value).append("</th>") })
        html.append("</tr></thead>")
    }

    //TODO optimize, quick hack
    private fun populateBody(html: StringBuilder, table: List<List<String?>>, appendRowNums: Boolean) {
        html.append("<tbody>")
        for (i in 1 until table.size) {
            val rowValues = table[i]
            var rowCss = ""
            if (rowValues.stream().anyMatch { anObject: String? -> "OK".equals(anObject) }) {
                rowCss = "success"
            } else if (rowValues.stream().anyMatch { anObject: String? -> "FAILED".equals(anObject) }) {
                rowCss = "danger"
            } else if (rowValues.stream().anyMatch { anObject: String? -> "SKIPPED".equals(anObject) }) {
                rowCss = "warning"
            }
            html.append("<tr class=\"").append(rowCss).append("\">")
            if (appendRowNums) {
                html.append("<td>").append(i).append("</td>")
            }
            rowValues.forEach(Consumer { value: String? -> html.append("<td>").append(value).append("</td>") })
            html.append("</tr>")
        }
        html.append("</tbody>")
    }

    private fun getTableCss(): String {
        val css = classpathFileResolver.getFileAsString("report/css/table.css")
        return """
            <style>
            $css
            </style>
                """.trimIndent()
    }
}