package com.shop.announcement.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.repository.AccountRepository;
import com.shop.announcement.dto.AnnouncementResponse;
import com.shop.announcement.dto.CreateAnnouncementRequest;
import com.shop.announcement.dto.UpdateAnnouncementRequest;
import com.shop.announcement.entity.Announcement;
import com.shop.announcement.entity.AnnouncementContentType;
import com.shop.announcement.entity.ImageOrientation;
import com.shop.announcement.exception.AnnouncementNotFoundException;
import com.shop.announcement.repository.AnnouncementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/** Unit tests for {@link AnnouncementServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class AnnouncementServiceImplTest {

    @Mock AnnouncementRepository announcementRepository;
    @Mock AccountRepository accountRepository;

    AnnouncementServiceImpl service;

    private static final UUID VENDOR_ID        = UUID.randomUUID();
    private static final UUID ANNOUNCEMENT_ID  = UUID.randomUUID();
    private static final String VENDOR_EMAIL   = "vendor@shop.com";

    @BeforeEach
    void setUp() {
        service = new AnnouncementServiceImpl(announcementRepository, accountRepository);
    }

    private Account vendor() {
        Account a = new Account();
        a.setEmail(VENDOR_EMAIL);
        return a;
    }

    private Announcement announcement(AnnouncementContentType type) {
        Account v = vendor();
        Announcement a = new Announcement();
        a.setVendor(v);
        a.setContentType(type);
        a.setTextContent("Hello");
        a.setActive(true);
        a.setSortOrder(0);
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());
        return a;
    }

    // --- create ---

    /** create must persist the announcement with orientation from the request and return a DTO. */
    @Test
    void create_persistsAnnouncementAndReturnsDto() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendor()));
        given(announcementRepository.save(any(Announcement.class))).willAnswer(inv -> inv.getArgument(0));

        CreateAnnouncementRequest req = new CreateAnnouncementRequest();
        req.setContentType(AnnouncementContentType.IMAGE);
        req.setImageUrl("/uploads/announcements/img.jpg");
        req.setImageOrientation(ImageOrientation.LANDSCAPE);
        req.setActive(true);

        AnnouncementResponse result = service.create(VENDOR_EMAIL, req);

        then(announcementRepository).should().save(any(Announcement.class));
        assertThat(result.getContentType()).isEqualTo(AnnouncementContentType.IMAGE);
        assertThat(result.getImageOrientation()).isEqualTo(ImageOrientation.LANDSCAPE);
        assertThat(result.isActive()).isTrue();
    }

    /** create with TEXT type must store the text content. */
    @Test
    void create_textType_storesTextContent() {
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(vendor()));
        given(announcementRepository.save(any(Announcement.class))).willAnswer(inv -> inv.getArgument(0));

        CreateAnnouncementRequest req = new CreateAnnouncementRequest();
        req.setContentType(AnnouncementContentType.TEXT);
        req.setTextContent("Summer sale!");

        AnnouncementResponse result = service.create(VENDOR_EMAIL, req);

        assertThat(result.getTextContent()).isEqualTo("Summer sale!");
        assertThat(result.getImageOrientation()).isNull();
    }

    // --- listForVendor ---

    /** listForVendor must return all announcements belonging to the vendor ordered by sort_order. */
    @Test
    void listForVendor_returnsDtosInOrder() {
        Account v = vendor();
        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(v));
        given(announcementRepository.findByVendorIdOrderBySortOrderAsc(v.getId()))
                .willReturn(List.of(announcement(AnnouncementContentType.TEXT), announcement(AnnouncementContentType.IMAGE)));

        List<AnnouncementResponse> result = service.listForVendor(VENDOR_EMAIL);

        assertThat(result).hasSize(2);
    }

    // --- update ---

    /** update must apply all fields and return the updated DTO. */
    @Test
    void update_appliesChangesAndReturnsDto() {
        Announcement existing = announcement(AnnouncementContentType.TEXT);
        given(announcementRepository.findById(ANNOUNCEMENT_ID)).willReturn(Optional.of(existing));
        given(announcementRepository.save(any(Announcement.class))).willAnswer(inv -> inv.getArgument(0));

        UpdateAnnouncementRequest req = new UpdateAnnouncementRequest();
        req.setContentType(AnnouncementContentType.BOTH);
        req.setTextContent("Updated text");
        req.setImageUrl("/uploads/announcements/new.jpg");
        req.setImageOrientation(ImageOrientation.PORTRAIT);
        req.setActive(false);

        AnnouncementResponse result = service.update(VENDOR_EMAIL, ANNOUNCEMENT_ID, req);

        assertThat(result.getContentType()).isEqualTo(AnnouncementContentType.BOTH);
        assertThat(result.getImageOrientation()).isEqualTo(ImageOrientation.PORTRAIT);
        assertThat(result.isActive()).isFalse();
    }

    /** update must throw AnnouncementNotFoundException when the id does not exist. */
    @Test
    void update_throwsAnnouncementNotFoundException_whenNotFound() {
        given(announcementRepository.findById(ANNOUNCEMENT_ID)).willReturn(Optional.empty());

        UpdateAnnouncementRequest req = new UpdateAnnouncementRequest();
        req.setContentType(AnnouncementContentType.TEXT);

        assertThatThrownBy(() -> service.update(VENDOR_EMAIL, ANNOUNCEMENT_ID, req))
                .isInstanceOf(AnnouncementNotFoundException.class);
    }

    /** update must throw AnnouncementNotFoundException when the announcement belongs to another vendor. */
    @Test
    void update_throwsAnnouncementNotFoundException_whenOwnedByOtherVendor() {
        Announcement other = announcement(AnnouncementContentType.TEXT);
        other.getVendor().setEmail("other@vendor.com");
        given(announcementRepository.findById(ANNOUNCEMENT_ID)).willReturn(Optional.of(other));

        UpdateAnnouncementRequest req = new UpdateAnnouncementRequest();
        req.setContentType(AnnouncementContentType.TEXT);

        assertThatThrownBy(() -> service.update(VENDOR_EMAIL, ANNOUNCEMENT_ID, req))
                .isInstanceOf(AnnouncementNotFoundException.class);
    }

    // --- delete ---

    /** delete must call repository.delete when the announcement exists and is owned by the vendor. */
    @Test
    void delete_deletesAnnouncementWhenOwned() {
        Announcement existing = announcement(AnnouncementContentType.TEXT);
        given(announcementRepository.findById(ANNOUNCEMENT_ID)).willReturn(Optional.of(existing));

        service.delete(VENDOR_EMAIL, ANNOUNCEMENT_ID);

        then(announcementRepository).should().delete(existing);
    }

    /** delete must throw AnnouncementNotFoundException when the announcement does not exist. */
    @Test
    void delete_throwsAnnouncementNotFoundException_whenNotFound() {
        given(announcementRepository.findById(ANNOUNCEMENT_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(VENDOR_EMAIL, ANNOUNCEMENT_ID))
                .isInstanceOf(AnnouncementNotFoundException.class);
    }

    // --- moveUp / moveDown ---

    /** moveUp must swap sort_order with the previous item. */
    @Test
    void moveUp_swapsSortOrderWithPreviousItem() {
        Account v = vendor();
        Announcement first  = announcement(AnnouncementContentType.TEXT); first.setSortOrder(0);
        Announcement second = announcement(AnnouncementContentType.TEXT); second.setSortOrder(1);
        // Give them deterministic IDs via reflection
        setId(first,  UUID.randomUUID());
        setId(second, ANNOUNCEMENT_ID);

        given(accountRepository.findByEmail(VENDOR_EMAIL)).willReturn(Optional.of(v));
        given(announcementRepository.findByVendorIdOrderBySortOrderAsc(v.getId()))
                .willReturn(List.of(first, second));
        given(announcementRepository.save(any(Announcement.class))).willAnswer(inv -> inv.getArgument(0));

        service.moveUp(VENDOR_EMAIL, ANNOUNCEMENT_ID);

        assertThat(second.getSortOrder()).isEqualTo(0);
        assertThat(first.getSortOrder()).isEqualTo(1);
    }

    /** listActive must return only active announcements. */
    @Test
    void listActive_returnsOnlyActiveAnnouncements() {
        Announcement active = announcement(AnnouncementContentType.TEXT);
        active.setActive(true);
        given(announcementRepository.findByActiveTrueOrderBySortOrderAsc())
                .willReturn(List.of(active));

        List<AnnouncementResponse> result = service.listActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isActive()).isTrue();
    }

    // --- helper ---

    private void setId(Announcement a, UUID id) {
        try {
            var field = Announcement.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(a, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
