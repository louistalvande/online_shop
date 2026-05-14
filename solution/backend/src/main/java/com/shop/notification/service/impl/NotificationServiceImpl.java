package com.shop.notification.service.impl;

import com.shop.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Stub — logs emails to console until SendGrid is configured (IFS-06). */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    /** {@inheritDoc} */
    @Override
    public void sendActivationEmail(String toEmail, String activationLink) {
        log.info("[EMAIL] Activation link for {} → {}", toEmail, activationLink);
    }
}
