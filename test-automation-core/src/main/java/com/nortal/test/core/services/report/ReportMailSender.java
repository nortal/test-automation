package com.nortal.test.core.services.report;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.nortal.test.core.configuration.MailProperties;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import com.nortal.test.core.configuration.ReportProperties;
import com.nortal.test.core.model.TargetEnvironment;
import com.nortal.test.core.services.report.html.MailSummaryPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This component is responsible for distributing test report via email.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReportMailSender {

	private final ReportProperties reportProperties;
	private final MailProperties mailProperties;
	private final JavaMailSender mailSender;
	private final Environment env;

	public void mail() {
		if (!mailProperties.isEnabled() || !reportProperties.shouldReport()) {
			log.info("Skipping email report sending as it is disabled.");
			return;
		}

		try {
			final Path mailSummaryPath = Paths.get(reportProperties.getOutput())
					.resolve(Paths.get("cucumber-html-reports/" + MailSummaryPage.WEB_PAGE));
			final String report = String.join("\n", Files.readAllLines(mailSummaryPath));

			final MimeMessage mimeMessage = mailSender.createMimeMessage();
			final MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, "utf-8");
			mimeMessageHelper.setFrom(mailProperties.getFrom());
			mimeMessageHelper.setTo(mailProperties.getTo());
			mimeMessageHelper.setSubject(
					String.format("%s (New) %s %s",
							getEnv(),
							reportProperties.getBuildName(),
					reportProperties.getBuildNumber()));
			mimeMessageHelper.setText(report, true);

			mailSender.send(mimeMessage);
		} catch (MessagingException | IOException e) {
			log.error("Failed to send post test run email report", e);
		}
	}

	private String getEnv() {
		return Arrays.stream(env.getActiveProfiles())
					.filter(TargetEnvironment::isAssignable)
					.map(String::toUpperCase)
					.map(TargetEnvironment::valueOf)
					.findFirst()
					.orElseThrow(() -> new IllegalStateException("There is no profile set for this test run!"))
					.toString();

	}
}
