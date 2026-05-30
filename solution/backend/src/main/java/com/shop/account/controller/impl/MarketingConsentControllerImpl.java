package com.shop.account.controller.impl;

import com.shop.account.controller.MarketingConsentController;
import com.shop.account.service.MarketingConsentService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/** Default implementation of {@link MarketingConsentController}. */
@RestController
public class MarketingConsentControllerImpl implements MarketingConsentController {

    private final MarketingConsentService marketingConsentService;

    /**
     * @param marketingConsentService the export service
     */
    public MarketingConsentControllerImpl(MarketingConsentService marketingConsentService) {
        this.marketingConsentService = marketingConsentService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> exportMailingList(Principal principal) {
        byte[] csv = marketingConsentService.exportMailingListCsv(principal.getName());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("mailing-list.csv").build()
        );

        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
