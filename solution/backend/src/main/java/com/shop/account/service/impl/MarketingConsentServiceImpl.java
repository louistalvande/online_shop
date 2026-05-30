package com.shop.account.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.entity.AccountRole;
import com.shop.account.entity.AccountStatus;
import com.shop.account.repository.AccountRepository;
import com.shop.account.service.MarketingConsentService;
import com.shop.audit.entity.AuditEventType;
import com.shop.audit.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

/** Default implementation of {@link MarketingConsentService}. */
@Service
@Transactional(readOnly = true)
public class MarketingConsentServiceImpl implements MarketingConsentService {

    private final AccountRepository accountRepository;
    private final AuditLogService auditLogService;

    /**
     * @param accountRepository data access for account queries
     * @param auditLogService   records the export event
     */
    public MarketingConsentServiceImpl(AccountRepository accountRepository,
                                       AuditLogService auditLogService) {
        this.accountRepository = accountRepository;
        this.auditLogService   = auditLogService;
    }

    /** {@inheritDoc} */
    @Override
    public byte[] exportMailingListCsv(String vendorEmail) {
        List<Account> consenting = accountRepository
                .findByRoleAndStatusAndMarketingConsentTrue(AccountRole.BUYER, AccountStatus.ACTIVE);

        StringBuilder csv = new StringBuilder("email,firstName,lastName\n");
        for (Account a : consenting) {
            csv.append(escapeCsv(a.getEmail())).append(',')
               .append(escapeCsv(a.getFirstName())).append(',')
               .append(escapeCsv(a.getLastName())).append('\n');
        }

        auditLogService.log(
                AuditEventType.MARKETING_CONSENT_EXPORT,
                vendorEmail,
                consenting.size() + " contacts exported"
        );

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /** Wraps a value in double quotes and escapes internal double quotes per RFC 4180. */
    private static String escapeCsv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
