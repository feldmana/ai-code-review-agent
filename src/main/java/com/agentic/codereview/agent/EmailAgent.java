package com.agentic.codereview.agent;

import com.agentic.codereview.config.AppConfig;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * EmailAgent sends code review reports via email
 */
public class EmailAgent {
    private static final Logger logger = LoggerFactory.getLogger(EmailAgent.class);
    private final AppConfig config;

    public EmailAgent(AppConfig config) {
        this.config = config;
    }

    /**
     * Sends a report via email
     */
    public boolean sendReport(String reportContent, String subject) {
        if (!config.isEmailEnabled()) {
            logger.warn("Email is disabled in configuration");
            return false;
        }

        if (config.getEmailTo() == null || config.getEmailTo().isEmpty()) {
            logger.error("Email recipient not configured");
            return false;
        }

        try {
            logger.info("Sending email report to: {}", config.getEmailTo());

            Properties properties = new Properties();
            properties.put("mail.smtp.host", config.getSmtpHost());
            properties.put("mail.smtp.port", config.getSmtpPort());
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.starttls.enable", config.isSmtpTlsEnabled());
            properties.put("mail.smtp.starttls.required", config.isSmtpTlsEnabled());

            Session session = Session.getInstance(properties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                            config.getSmtpUsername(),
                            config.getSmtpPassword()
                    );
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.getSmtpUsername()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(config.getEmailTo()));
            message.setSubject(subject);

            // Create multipart message for HTML and plain text
            MimeMultipart multipart = new MimeMultipart("alternative");

            // Plain text version
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(reportContent, "utf-8", "plain");
            multipart.addBodyPart(textPart);

            // HTML version (converted from markdown-like format)
            MimeBodyPart htmlPart = new MimeBodyPart();
            String htmlContent = convertToHtml(reportContent);
            htmlPart.setContent(htmlContent, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            message.setContent(multipart);

            Transport.send(message);
            logger.info("Email sent successfully to: {}", config.getEmailTo());
            return true;

        } catch (MessagingException e) {
            logger.error("Failed to send email: {}", e.getMessage(), e);
            System.out.println("\n⚠️  Report sending failed!");
            System.out.println("📄 However, your report has been saved successfully!");
            System.out.println("📂 You can find the report at: reports/code_review_report_*.md");
            System.out.println("   Location: /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/reports/\n");
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error while sending email: {}", e.getMessage(), e);
            System.out.println("\n⚠️  Report sending failed!");
            System.out.println("📄 However, your report has been saved successfully!");
            System.out.println("📂 You can find the report at: reports/code_review_report_*.md");
            System.out.println("   Location: /Users/alexandrafeldman/Documents/Learning/OpenAI/CodeReviewAgent/reports/\n");
            return false;
        }
    }

    /**
     * Converts markdown-like text to HTML
     */
    private String convertToHtml(String markdownText) {
        String html = markdownText
                .replace("# ", "<h1>")
                .replace("## ", "<h2>")
                .replace("### ", "<h3>")
                .replace("\n", "<br/>\n")
                .replace("<h1>", "<h1>")
                .replace("<h2>", "<h2>")
                .replace("<h3>", "<h3>")
                .replace("- ", "<li>");

        return "<html><body style='font-family: Arial, sans-serif;'>" + html + "</body></html>";
    }

    /**
     * Validates email configuration
     */
    public boolean validateConfiguration() {
        boolean valid = config.isEmailEnabled() &&
                config.getEmailTo() != null && !config.getEmailTo().isEmpty() &&
                config.getSmtpUsername() != null && !config.getSmtpUsername().isEmpty() &&
                config.getSmtpPassword() != null && !config.getSmtpPassword().isEmpty();

        if (!valid) {
            logger.warn("Email configuration is incomplete or disabled");
        }

        return valid;
    }
}

