package com.shop.notification.service.impl;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.shop.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Locale;

/**
 * Sends transactional emails via SendGrid (IFS-06).
 * Falls back to console logging when {@code SENDGRID_API_KEY} is not set (dev/test mode).
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final SendGrid sendGrid;
    private final String fromEmail;
    private final String fromName;
    private final int activationExpiryHours;
    private final MessageSource messageSource;
    private final boolean sendGridConfigured;

    /**
     * @param apiKey                SendGrid API key — leave blank in dev to skip real sends
     * @param fromEmail             verified sender address registered in SendGrid
     * @param fromName              display name shown in the From field
     * @param activationExpiryHours token TTL injected to include in the email body
     * @param messageSource         Spring message source for localised email content
     */
    public NotificationServiceImpl(
            @Value("${sendgrid.api-key:}") String apiKey,
            @Value("${sendgrid.from-email:noreply@shop.example.com}") String fromEmail,
            @Value("${sendgrid.from-name:Online Shop}") String fromName,
            @Value("${app.activation-expiry-hours}") int activationExpiryHours,
            MessageSource messageSource) {
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.activationExpiryHours = activationExpiryHours;
        this.messageSource = messageSource;
        this.sendGridConfigured = !apiKey.isBlank();
        this.sendGrid = sendGridConfigured ? new SendGrid(apiKey) : null;
    }

    /** {@inheritDoc} */
    @Override
    public void sendActivationEmail(String toEmail, String activationLink, Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Activation link for {} → {}", toEmail, activationLink);
            return;
        }

        String subject = messageSource.getMessage("email.activation.subject", null, locale);
        String htmlBody = messageSource.getMessage(
                "email.activation.body.html",
                new Object[]{activationLink, activationExpiryHours},
                locale);

        Mail mail = new Mail(
                new Email(fromEmail, fromName),
                subject,
                new Email(toEmail),
                new Content("text/html", htmlBody));

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            if (response.getStatusCode() >= 400) {
                log.warn("[EMAIL] SendGrid rejected activation email for {} — status {} body {}",
                        toEmail, response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            log.warn("[EMAIL] Failed to send activation email to {}: {}", toEmail, e.getMessage());
        }
    }
}
