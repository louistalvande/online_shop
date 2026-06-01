package com.shop.account.service;

/**
 * Builds and returns the marketing mailing list for the authenticated vendor (US-PRF-05 / RGPD-CONS-004).
 */
public interface MarketingConsentService {

    /**
     * Generates a CSV byte array containing email, firstName and lastName of every active buyer
     * who has opted in to marketing emails, then records the export in the audit log.
     *
     * @param vendorEmail the email of the vendor triggering the export (used for audit)
     * @return UTF-8 CSV bytes with a header row followed by one data row per consenting buyer
     */
    byte[] exportMailingListCsv(String vendorEmail);
}
