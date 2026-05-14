package com.shop.notification.service;

/** Sends transactional emails to platform users. */
public interface NotificationService {

    /**
     * Sends an activation email containing the link to set a password and activate the account.
     * Used for both admin-created accounts (US-ADM-01) and buyer self-registration (US-REG-01).
     *
     * @param toEmail        the recipient email address
     * @param activationLink the full activation URL including the token
     */
    void sendActivationEmail(String toEmail, String activationLink);
}
