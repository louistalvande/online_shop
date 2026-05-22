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

    static final String KEY_MAINTENANCE = "maintenance_mode";

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
}
