package com.nortal.test.core.services.report

import com.nortal.test.core.configuration.MailProperties
import com.nortal.test.core.configuration.ReportProperties
import com.nortal.test.core.model.TargetEnvironment
import com.nortal.test.core.services.report.html.MailSummaryPage
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.core.env.Environment
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import java.io.IOException
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.mail.MessagingException

/**
 * This component is responsible for distributing test report via email.
 */
@Slf4j
@Component
@RequiredArgsConstructor
class ReportMailSender {
    private val reportProperties: ReportProperties? = null
    private val mailProperties: MailProperties? = null
    private val mailSender: JavaMailSender? = null
    private val env: Environment? = null
    fun mail() {
        if (!mailProperties!!.isEnabled || !reportProperties!!.shouldReport()) {
            ReportMailSender.log.info("Skipping email report sending as it is disabled.")
            return
        }
        try {
            val mailSummaryPath = Paths.get(reportProperties.output)
                .resolve(Paths.get("cucumber-html-reports/" + MailSummaryPage.WEB_PAGE))
            val report = java.lang.String.join("\n", Files.readAllLines(mailSummaryPath))
            val mimeMessage = mailSender!!.createMimeMessage()
            val mimeMessageHelper = MimeMessageHelper(mimeMessage, "utf-8")
            mimeMessageHelper.setFrom(mailProperties.from)
            mimeMessageHelper.setTo(mailProperties.to)
            mimeMessageHelper.setSubject(
                String.format(
                    "%s (New) %s %s",
                    getEnv(),
                    reportProperties!!.buildName,
                    reportProperties.buildNumber
                )
            )
            mimeMessageHelper.setText(report, true)
            mailSender.send(mimeMessage)
        } catch (e: MessagingException) {
            ReportMailSender.log.error("Failed to send post test run email report", e)
        } catch (e: IOException) {
            ReportMailSender.log.error("Failed to send post test run email report", e)
        }
    }

    private fun getEnv(): String {
        return Arrays.stream(env!!.activeProfiles)
            .filter { profile: String? -> TargetEnvironment.isAssignable(profile) }
            .map { obj: String -> obj.uppercase(Locale.getDefault()) }
            .map { name: String? ->
                TargetEnvironment.valueOf(
                    name!!
                )
            }
            .findFirst()
            .orElseThrow { IllegalStateException("There is no profile set for this test run!") }
            .toString()
    }
}