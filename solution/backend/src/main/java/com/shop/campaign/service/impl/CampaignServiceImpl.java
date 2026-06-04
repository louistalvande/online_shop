package com.shop.campaign.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountLanguage;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import com.shop.account.repository.AccountRepository;
import com.shop.campaign.dto.CampaignRecipientsCountResponse;
import com.shop.campaign.dto.CampaignSentResponse;
import com.shop.campaign.dto.SendCampaignRequest;
import com.shop.campaign.entity.MarketingCampaign;
import com.shop.campaign.exception.NoConsentingBuyersException;
import com.shop.campaign.repository.MarketingCampaignRepository;
import com.shop.campaign.service.CampaignService;
import com.shop.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Implements marketing campaign dispatch (US-MKTG-01 / FS-V17).
 * Fetches all active consenting buyers, sends each an email via {@link NotificationService},
 * and persists a log entry in {@code marketing_campaigns}.
 */
@Service
@Transactional
public class CampaignServiceImpl implements CampaignService {

    private final AccountRepository accountRepository;
    private final MarketingCampaignRepository campaignRepository;
    private final NotificationService notificationService;

    /**
     * @param accountRepository   repository for fetching consenting buyers
     * @param campaignRepository  repository for persisting campaign log entries
     * @param notificationService service for sending individual emails
     */
    public CampaignServiceImpl(AccountRepository accountRepository,
                                MarketingCampaignRepository campaignRepository,
                                NotificationService notificationService) {
        this.accountRepository = accountRepository;
        this.campaignRepository = campaignRepository;
        this.notificationService = notificationService;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public CampaignRecipientsCountResponse getRecipientsCount(String vendorEmail) {
        List<Account> recipients = accountRepository
                .findByRoleAndStatusAndMarketingConsentTrue(AccountRole.BUYER, AccountStatus.ACTIVE);
        CampaignRecipientsCountResponse response = new CampaignRecipientsCountResponse();
        response.setCount(recipients.size());
        return response;
    }

    /** {@inheritDoc} */
    @Override
    public CampaignSentResponse sendCampaign(String vendorEmail, SendCampaignRequest request) {
        List<Account> recipients = accountRepository
                .findByRoleAndStatusAndMarketingConsentTrue(AccountRole.BUYER, AccountStatus.ACTIVE);

        if (recipients.isEmpty()) {
            throw new NoConsentingBuyersException();
        }

        Account vendor = accountRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new IllegalStateException("Vendor not found: " + vendorEmail));

        for (Account buyer : recipients) {
            AccountLanguage lang = buyer.getLanguage() != null ? buyer.getLanguage() : AccountLanguage.FR;
            Locale locale = Locale.forLanguageTag(lang.name().toLowerCase());
            notificationService.sendMarketingCampaignEmail(buyer.getEmail(), request.getSubject(), request.getBody(), locale);
        }

        OffsetDateTime sentAt = OffsetDateTime.now();

        MarketingCampaign campaign = new MarketingCampaign();
        campaign.setVendor(vendor);
        campaign.setSubject(request.getSubject());
        campaign.setBody(request.getBody());
        campaign.setRecipientCount(recipients.size());
        campaign.setStatus("SUCCESS");
        campaign.setSentAt(sentAt);
        MarketingCampaign saved = campaignRepository.save(campaign);

        CampaignSentResponse response = new CampaignSentResponse();
        response.setId(saved.getId());
        response.setRecipientCount(saved.getRecipientCount());
        response.setSentAt(saved.getSentAt());
        return response;
    }
}
