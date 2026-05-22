package com.shop.settings.service.impl;

import com.shop.settings.dto.MaintenanceStatusResponse;
import com.shop.settings.entity.PlatformSetting;
import com.shop.settings.exception.SettingNotFoundException;
import com.shop.settings.repository.PlatformSettingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link SettingsServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class SettingsServiceImplTest {

    @Mock PlatformSettingRepository settingRepository;

    SettingsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SettingsServiceImpl(settingRepository);
    }

    private PlatformSetting setting(String value) {
        PlatformSetting s = new PlatformSetting();
        s.setKey(SettingsServiceImpl.KEY_MAINTENANCE);
        s.setValue(value);
        return s;
    }

    /** getMaintenanceStatus must return active=false when the setting is false. */
    @Test
    void getMaintenanceStatus_returnsFalse_whenSettingIsFalse() {
        given(settingRepository.findById(SettingsServiceImpl.KEY_MAINTENANCE))
                .willReturn(Optional.of(setting("false")));

        MaintenanceStatusResponse result = service.getMaintenanceStatus();

        assertThat(result.isActive()).isFalse();
    }

    /** getMaintenanceStatus must return active=true when the setting is true. */
    @Test
    void getMaintenanceStatus_returnsTrue_whenSettingIsTrue() {
        given(settingRepository.findById(SettingsServiceImpl.KEY_MAINTENANCE))
                .willReturn(Optional.of(setting("true")));

        MaintenanceStatusResponse result = service.getMaintenanceStatus();

        assertThat(result.isActive()).isTrue();
    }

    /** isMaintenanceActive must return false when the setting row does not exist. */
    @Test
    void isMaintenanceActive_returnsFalse_whenSettingMissing() {
        given(settingRepository.findById(SettingsServiceImpl.KEY_MAINTENANCE))
                .willReturn(Optional.empty());

        assertThat(service.isMaintenanceActive()).isFalse();
    }

    /** setMaintenanceMode must persist the new value and return the updated status. */
    @Test
    void setMaintenanceMode_updatesSettingAndReturnsDto_whenTrue() {
        PlatformSetting existing = setting("false");
        given(settingRepository.findById(SettingsServiceImpl.KEY_MAINTENANCE))
                .willReturn(Optional.of(existing));
        given(settingRepository.save(any(PlatformSetting.class))).willAnswer(inv -> inv.getArgument(0));

        MaintenanceStatusResponse result = service.setMaintenanceMode(true);

        assertThat(result.isActive()).isTrue();
        then(settingRepository).should().save(existing);
    }

    /** setMaintenanceMode must persist false and return active=false. */
    @Test
    void setMaintenanceMode_updatesSettingAndReturnsDto_whenFalse() {
        PlatformSetting existing = setting("true");
        given(settingRepository.findById(SettingsServiceImpl.KEY_MAINTENANCE))
                .willReturn(Optional.of(existing));
        given(settingRepository.save(any(PlatformSetting.class))).willAnswer(inv -> inv.getArgument(0));

        MaintenanceStatusResponse result = service.setMaintenanceMode(false);

        assertThat(result.isActive()).isFalse();
    }

    /** setMaintenanceMode must throw SettingNotFoundException when the row is missing. */
    @Test
    void setMaintenanceMode_throwsSettingNotFoundException_whenRowMissing() {
        given(settingRepository.findById(SettingsServiceImpl.KEY_MAINTENANCE))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> service.setMaintenanceMode(true))
                .isInstanceOf(SettingNotFoundException.class);
    }
}
