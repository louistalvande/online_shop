package com.shop.announcement.controller.impl;

import com.shop.announcement.dto.AnnouncementResponse;
import com.shop.announcement.dto.CreateAnnouncementRequest;
import com.shop.announcement.dto.UpdateAnnouncementRequest;
import com.shop.announcement.entity.AnnouncementContentType;
import com.shop.announcement.entity.ImageOrientation;
import com.shop.announcement.exception.AnnouncementNotFoundException;
import com.shop.announcement.service.AnnouncementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

/** Unit tests for {@link VendorAnnouncementControllerImpl}. */
@ExtendWith(MockitoExtension.class)
class VendorAnnouncementControllerImplTest {

    @Mock AnnouncementService announcementService;
    @Mock Principal principal;

    VendorAnnouncementControllerImpl controller;

    private static final UUID ID          = UUID.randomUUID();
    private static final String VENDOR    = "vendor@shop.com";

    @BeforeEach
    void setUp() {
        controller = new VendorAnnouncementControllerImpl(announcementService);
        given(principal.getName()).willReturn(VENDOR);
    }

    private AnnouncementResponse dto(AnnouncementContentType type) {
        return AnnouncementResponse.from(buildAnnouncement(type));
    }

    private com.shop.announcement.entity.Announcement buildAnnouncement(AnnouncementContentType type) {
        com.shop.account.entity.Account vendor = new com.shop.account.entity.Account();
        vendor.setEmail(VENDOR);
        com.shop.announcement.entity.Announcement a = new com.shop.announcement.entity.Announcement();
        a.setVendor(vendor);
        a.setContentType(type);
        a.setActive(true);
        a.setSortOrder(0);
        a.setCreatedAt(java.time.LocalDateTime.now());
        a.setUpdatedAt(java.time.LocalDateTime.now());
        return a;
    }

    /** list must return HTTP 200 with the vendor's announcements. */
    @Test
    void list_returns200WithDtoList() {
        given(announcementService.listForVendor(VENDOR)).willReturn(List.of(dto(AnnouncementContentType.TEXT)));

        ResponseEntity<List<AnnouncementResponse>> response = controller.list(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    /** create must return HTTP 201 with the created DTO. */
    @Test
    void create_returns201WithCreatedDto() {
        CreateAnnouncementRequest req = new CreateAnnouncementRequest();
        req.setContentType(AnnouncementContentType.IMAGE);
        req.setImageUrl("/uploads/announcements/img.jpg");
        req.setImageOrientation(ImageOrientation.LANDSCAPE);

        given(announcementService.create(VENDOR, req)).willReturn(dto(AnnouncementContentType.IMAGE));

        ResponseEntity<AnnouncementResponse> response = controller.create(principal, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
    }

    /** update must return HTTP 200 with the updated DTO. */
    @Test
    void update_returns200WithUpdatedDto() {
        UpdateAnnouncementRequest req = new UpdateAnnouncementRequest();
        req.setContentType(AnnouncementContentType.TEXT);
        req.setTextContent("New text");

        given(announcementService.update(VENDOR, ID, req)).willReturn(dto(AnnouncementContentType.TEXT));

        ResponseEntity<AnnouncementResponse> response = controller.update(principal, ID, req);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /** update must propagate AnnouncementNotFoundException from the service. */
    @Test
    void update_propagatesAnnouncementNotFoundException() {
        UpdateAnnouncementRequest req = new UpdateAnnouncementRequest();
        req.setContentType(AnnouncementContentType.TEXT);

        given(announcementService.update(VENDOR, ID, req))
                .willThrow(new AnnouncementNotFoundException(ID));

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.update(principal, ID, req))
                .isInstanceOf(AnnouncementNotFoundException.class);
    }

    /** delete must return HTTP 204 and call the service. */
    @Test
    void delete_returns204AndCallsService() {
        ResponseEntity<Void> response = controller.delete(principal, ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        then(announcementService).should().delete(VENDOR, ID);
    }

    /** moveUp must return HTTP 200 with the updated DTO. */
    @Test
    void moveUp_returns200() {
        given(announcementService.moveUp(VENDOR, ID)).willReturn(dto(AnnouncementContentType.TEXT));

        ResponseEntity<AnnouncementResponse> response = controller.moveUp(principal, ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /** moveDown must return HTTP 200 with the updated DTO. */
    @Test
    void moveDown_returns200() {
        given(announcementService.moveDown(VENDOR, ID)).willReturn(dto(AnnouncementContentType.TEXT));

        ResponseEntity<AnnouncementResponse> response = controller.moveDown(principal, ID);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
