package com.shop.notification.service.impl;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.shop.notification.service.NotificationService;
import com.shop.order.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
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
    @Async
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

        sendEmail(toEmail, subject, htmlBody);
    }

    /** {@inheritDoc} */
    @Async
    @Override
    public void sendOrderConfirmationEmail(String toEmail, OrderResponse order, Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Order confirmation for {} — order #{}", toEmail, order.getOrderNumber());
            return;
        }

        String subject = messageSource.getMessage(
                "email.order.confirmation.subject",
                new Object[]{order.getOrderNumber()},
                locale);
        String htmlBody = messageSource.getMessage(
                "email.order.confirmation.body.html",
                new Object[]{order.getOrderNumber(), order.getTotalAmountTtc()},
                locale);

        sendEmail(toEmail, subject, htmlBody);
    }

    /** {@inheritDoc} */
    @Async
    @Override
    public void sendWireTransferDetailsEmail(String toEmail, String orderNumber,
                                             BigDecimal totalAmountTtc,
                                             String bankIban, String bankBic,
                                             Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Wire transfer details for {} — order #{} amount={} IBAN={} BIC={}",
                    toEmail, orderNumber, totalAmountTtc, bankIban, bankBic);
            return;
        }

        String subject = messageSource.getMessage(
                "email.wire.transfer.subject",
                new Object[]{orderNumber},
                locale);
        String htmlBody = messageSource.getMessage(
                "email.wire.transfer.body.html",
                new Object[]{orderNumber, totalAmountTtc, bankIban, bankBic},
                locale);

        sendEmail(toEmail, subject, htmlBody);
    }

    /** {@inheritDoc} */
    @Async
    @Override
    public void sendVendorNewOrderEmail(String toEmail, OrderResponse order, Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Vendor new order notification for {} — order #{}", toEmail, order.getOrderNumber());
            return;
        }

        String subject = messageSource.getMessage(
                "email.vendor.new.order.subject",
                new Object[]{order.getOrderNumber()},
                locale);
        String htmlBody = messageSource.getMessage(
                "email.vendor.new.order.body.html",
                new Object[]{order.getOrderNumber(), order.getTotalAmountTtc()},
                locale);

        sendEmail(toEmail, subject, htmlBody);
    }

    /** {@inheritDoc} */
    @Async
    @Override
    public void sendShipmentNotificationEmail(String toEmail, OrderResponse order, Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Shipment notification for {} — order #{} tracking={}",
                    toEmail, order.getOrderNumber(), order.getTrackingNumber());
            return;
        }

        String subject = messageSource.getMessage(
                "email.shipment.subject",
                new Object[]{order.getOrderNumber()},
                locale);
        String htmlBody = messageSource.getMessage(
                "email.shipment.body.html",
                new Object[]{order.getOrderNumber(), order.getTrackingNumber(),
                        order.getCarrierTrackingUrl(), order.getCarrierName()},
                locale);

        sendEmail(toEmail, subject, htmlBody);
    }

    /** {@inheritDoc} */
    @Async
    @Override
    public void sendWirePaymentRejectedEmail(String toEmail, OrderResponse order, Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Wire payment rejected for {} — order #{}", toEmail, order.getOrderNumber());
            return;
        }

        String subject = messageSource.getMessage(
                "email.wire.rejected.subject",
                new Object[]{order.getOrderNumber()},
                locale);
        String htmlBody = messageSource.getMessage(
                "email.wire.rejected.body.html",
                new Object[]{order.getOrderNumber()},
                locale);

        sendEmail(toEmail, subject, htmlBody);
    }

    /** {@inheritDoc} */
    @Async
    @Override
    public void sendBuyerCancellationEmail(String toEmail, OrderResponse order, Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Buyer cancellation for {} — order #{} status={}", toEmail, order.getOrderNumber(), order.getStatus());
            return;
        }
        String subject = messageSource.getMessage("email.cancellation.buyer.subject", new Object[]{order.getOrderNumber()}, locale);
        String htmlBody = messageSource.getMessage("email.cancellation.buyer.body.html", new Object[]{order.getOrderNumber(), order.getStatus()}, locale);
        sendEmail(toEmail, subject, htmlBody);
    }

    /** {@inheritDoc} */
    @Async
    @Override
    public void sendVendorCancellationEmail(String toEmail, OrderResponse order, Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Vendor cancellation notification for {} — order #{}", toEmail, order.getOrderNumber());
            return;
        }
        String subject = messageSource.getMessage("email.cancellation.vendor.subject", new Object[]{order.getOrderNumber()}, locale);
        String htmlBody = messageSource.getMessage("email.cancellation.vendor.body.html", new Object[]{order.getOrderNumber()}, locale);
        sendEmail(toEmail, subject, htmlBody);
    }

    /** {@inheritDoc} */
    @Async
    @Override
    public void sendReturnRequestedEmail(String toEmail, OrderResponse order, Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Return requested for {} — order #{}", toEmail, order.getOrderNumber());
            return;
        }
        String subject = messageSource.getMessage("email.return.requested.subject", new Object[]{order.getOrderNumber()}, locale);
        String htmlBody = messageSource.getMessage("email.return.requested.body.html", new Object[]{order.getOrderNumber()}, locale);
        sendEmail(toEmail, subject, htmlBody);
    }

    /** {@inheritDoc} */
    @Async
    @Override
    public void sendWireRefundConfirmedEmail(String toEmail, OrderResponse order, Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Wire refund confirmed for {} — order #{}", toEmail, order.getOrderNumber());
            return;
        }
        String subject = messageSource.getMessage("email.wire.refund.confirmed.subject", new Object[]{order.getOrderNumber()}, locale);
        String htmlBody = messageSource.getMessage("email.wire.refund.confirmed.body.html", new Object[]{order.getOrderNumber()}, locale);
        sendEmail(toEmail, subject, htmlBody);
    }

    /** {@inheritDoc} */
    @Async
    @Override
    public void sendVendorCancellationRequestedEmail(String toEmail, OrderResponse order, Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Post-shipment cancellation request for vendor {} — order #{} reason={}",
                    toEmail, order.getOrderNumber(), order.getCancellationReason());
            return;
        }
        String subject = messageSource.getMessage("email.cancellation.requested.vendor.subject",
                new Object[]{order.getOrderNumber()}, locale);
        String htmlBody = messageSource.getMessage("email.cancellation.requested.vendor.body.html",
                new Object[]{order.getOrderNumber(), order.getCancellationReason()}, locale);
        sendEmail(toEmail, subject, htmlBody);
    }

    /** {@inheritDoc} */
    @Async
    @Override
    public void sendCancellationRefusedEmail(String toEmail, OrderResponse order, Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Cancellation refused notification for buyer {} — order #{}", toEmail, order.getOrderNumber());
            return;
        }
        String subject = messageSource.getMessage("email.cancellation.refused.buyer.subject",
                new Object[]{order.getOrderNumber()}, locale);
        String htmlBody = messageSource.getMessage("email.cancellation.refused.buyer.body.html",
                new Object[]{order.getOrderNumber()}, locale);
        sendEmail(toEmail, subject, htmlBody);
    }

    /** {@inheritDoc} */
    @Async
    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink, Locale locale) {
        if (!sendGridConfigured) {
            log.info("[EMAIL] Password reset link for {} → {}", toEmail, resetLink);
            return;
        }
        String subject = messageSource.getMessage("email.password.reset.subject", null, locale);
        String htmlBody = messageSource.getMessage(
                "email.password.reset.body.html",
                new Object[]{resetLink},
                locale);
        sendEmail(toEmail, subject, htmlBody);
    }

    /**
     * Sends an HTML email via SendGrid and logs a warning on failure.
     *
     * @param toEmail  recipient address
     * @param subject  email subject
     * @param htmlBody HTML email body
     */
    private void sendEmail(String toEmail, String subject, String htmlBody) {
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
                log.warn("[EMAIL] SendGrid rejected email to {} — status {} body {}",
                        toEmail, response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            log.warn("[EMAIL] Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }
}
