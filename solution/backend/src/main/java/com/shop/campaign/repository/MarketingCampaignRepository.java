package com.shop.campaign.repository;

import com.shop.campaign.entity.MarketingCampaign;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Data access layer for {@link MarketingCampaign} records. */
public interface MarketingCampaignRepository extends JpaRepository<MarketingCampaign, UUID> {
}
