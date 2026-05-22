package com.shop.settings.repository;

import com.shop.settings.entity.PlatformSetting;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for platform configuration settings. */
public interface PlatformSettingRepository extends JpaRepository<PlatformSetting, String> {
}
