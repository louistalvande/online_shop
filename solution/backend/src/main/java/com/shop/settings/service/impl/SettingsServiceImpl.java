package com.shop.settings.service.impl;

import com.shop.settings.dto.MaintenanceStatusResponse;
import com.shop.settings.entity.PlatformSetting;
import com.shop.settings.exception.SettingNotFoundException;
import com.shop.settings.repository.PlatformSettingRepository;
import com.shop.settings.service.SettingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** {@link SettingsService} implementation backed by the {@code platform_settings} table. */
@Service
@Transactional
public class SettingsServiceImpl implements SettingsService {

    static final String KEY_MAINTENANCE  = "maintenance_mode";
    static final String KEY_ACCENT_COLOR = "shop_accent_color";
    static final String KEY_BG_COLOR     = "shop_bg_color";
    static final String DEFAULT_ACCENT   = "#4e8b82";
    static final String DEFAULT_BG       = "#f2f6f5";

    private final PlatformSettingRepository settingRepository;

    /**
     * Constructs the service with its required repository.
     *
     * @param settingRepository the platform settings JPA repository
     */
    public SettingsServiceImpl(PlatformSettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public MaintenanceStatusResponse getMaintenanceStatus() {
        return new MaintenanceStatusResponse(isMaintenanceActive());
    }

    /** {@inheritDoc} */
    @Override
    public MaintenanceStatusResponse setMaintenanceMode(boolean active) {
        PlatformSetting setting = settingRepository.findById(KEY_MAINTENANCE)
                .orElseThrow(() -> new SettingNotFoundException(KEY_MAINTENANCE));
        setting.setValue(String.valueOf(active));
        settingRepository.save(setting);
        return new MaintenanceStatusResponse(active);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public boolean isMaintenanceActive() {
        return settingRepository.findById(KEY_MAINTENANCE)
                .map(s -> Boolean.parseBoolean(s.getValue()))
                .orElse(false);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public String getAccentColor() {
        return settingRepository.findById(KEY_ACCENT_COLOR)
                .map(PlatformSetting::getValue)
                .orElse(DEFAULT_ACCENT);
    }

    /** {@inheritDoc} */
    @Override
    public void setAccentColor(String accentColor) {
        PlatformSetting setting = settingRepository.findById(KEY_ACCENT_COLOR)
                .orElseGet(() -> { PlatformSetting s = new PlatformSetting(); s.setKey(KEY_ACCENT_COLOR); return s; });
        setting.setValue(accentColor);
        settingRepository.save(setting);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public String getBgColor() {
        return settingRepository.findById(KEY_BG_COLOR)
                .map(PlatformSetting::getValue)
                .orElse(DEFAULT_BG);
    }

    /** {@inheritDoc} */
    @Override
    public void setBgColor(String bgColor) {
        PlatformSetting setting = settingRepository.findById(KEY_BG_COLOR)
                .orElseGet(() -> { PlatformSetting s = new PlatformSetting(); s.setKey(KEY_BG_COLOR); return s; });
        setting.setValue(bgColor);
        settingRepository.save(setting);
    }
}
