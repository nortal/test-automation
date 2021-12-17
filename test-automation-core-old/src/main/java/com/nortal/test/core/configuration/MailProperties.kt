package com.nortal.test.core.configuration

import lombok.Setter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.lang.IllegalStateException
import java.util.*

@Component
@Setter
@ConfigurationProperties(prefix = "report.mail")
class MailProperties {
    /**
     * Whether sending report email is endabled
     * @return true if enabled
     */
    val isEnabled = false

    /**
     * Mail address from which the test report email is sent.
     * @return email address
     */
    val from: String? = null
    private val to: String? = null

    /**
     * Returns an array of email report recipient emails.
     * @return array of emails
     */
    fun getTo(): Array<String> {
        checkNotNull(to) { "Attempting to send email report without having report.mail.to property set!" }
        return Arrays.stream(if (to.contains(",")) to.split(",").toTypedArray() else to.split("\\s").toTypedArray())
            .map { obj: String -> obj.trim { it <= ' ' } }
            .toArray { _Dummy_.__Array__() }
    }
}