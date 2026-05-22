package com.shop.settings.controller.impl;

import com.shop.common.GlobalExceptionHandler;
import com.shop.settings.dto.MaintenanceStatusResponse;
import com.shop.settings.service.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Unit tests for {@link SettingsControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class SettingsControllerImplTest {

    @Mock SettingsService settingsService;
    @Mock MessageSource messageSource;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
                .standaloneSetup(new SettingsControllerImpl(settingsService))
                .setControllerAdvice(new GlobalExceptionHandler(messageSource))
                .build();
    }

    /** GET /api/public/maintenance returns 200 with active=false when maintenance is off. */
    @Test
    void getMaintenanceStatus_returns200WithFalse_whenInactive() throws Exception {
        given(settingsService.getMaintenanceStatus()).willReturn(new MaintenanceStatusResponse(false));

        mvc.perform(get("/api/public/maintenance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    /** GET /api/public/maintenance returns 200 with active=true when maintenance is on. */
    @Test
    void getMaintenanceStatus_returns200WithTrue_whenActive() throws Exception {
        given(settingsService.getMaintenanceStatus()).willReturn(new MaintenanceStatusResponse(true));

        mvc.perform(get("/api/public/maintenance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    /** GET /api/admin/settings/maintenance returns 200. */
    @Test
    void getMaintenanceStatusAdmin_returns200() throws Exception {
        given(settingsService.getMaintenanceStatus()).willReturn(new MaintenanceStatusResponse(false));

        mvc.perform(get("/api/admin/settings/maintenance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    /** PATCH /api/admin/settings/maintenance with active=true returns 200 with active=true. */
    @Test
    void setMaintenanceMode_returns200_whenEnabling() throws Exception {
        given(settingsService.setMaintenanceMode(true)).willReturn(new MaintenanceStatusResponse(true));

        mvc.perform(patch("/api/admin/settings/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    /** PATCH /api/admin/settings/maintenance with active=false returns 200 with active=false. */
    @Test
    void setMaintenanceMode_returns200_whenDisabling() throws Exception {
        given(settingsService.setMaintenanceMode(false)).willReturn(new MaintenanceStatusResponse(false));

        mvc.perform(patch("/api/admin/settings/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    /** PATCH /api/admin/settings/maintenance with missing body returns 400. */
    @Test
    void setMaintenanceMode_returns400_whenBodyMissing() throws Exception {
        mvc.perform(patch("/api/admin/settings/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
