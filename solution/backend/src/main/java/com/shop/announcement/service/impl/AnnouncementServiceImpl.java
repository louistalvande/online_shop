package com.shop.announcement.service.impl;

import com.shop.account.entity.Account;
import com.shop.account.repository.AccountRepository;
import com.shop.announcement.dto.AnnouncementResponse;
import com.shop.announcement.dto.CreateAnnouncementRequest;
import com.shop.announcement.dto.UpdateAnnouncementRequest;
import com.shop.announcement.entity.Announcement;
import com.shop.announcement.exception.AnnouncementNotFoundException;
import com.shop.announcement.repository.AnnouncementRepository;
import com.shop.announcement.service.AnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** {@link AnnouncementService} implementation. */
@Service
@Transactional
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AccountRepository accountRepository;

    /**
     * Constructs the service with its required repositories.
     *
     * @param announcementRepository the announcement JPA repository
     * @param accountRepository      the account JPA repository (used to resolve vendor by email)
     */
    public AnnouncementServiceImpl(AnnouncementRepository announcementRepository,
                                   AccountRepository accountRepository) {
        this.announcementRepository = announcementRepository;
        this.accountRepository = accountRepository;
    }

    /** {@inheritDoc} */
    @Override
    public AnnouncementResponse create(String vendorEmail, CreateAnnouncementRequest request) {
        Account vendor = accountRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new IllegalStateException("Vendor not found: " + vendorEmail));

        Announcement a = new Announcement();
        a.setVendor(vendor);
        a.setContentType(request.getContentType());
        a.setTextContent(request.getTextContent());
        a.setImageUrl(request.getImageUrl());
        a.setImageOrientation(request.getImageOrientation());
        a.setRedirectUrl(request.getRedirectUrl());
        a.setSortOrder(request.getSortOrder());
        a.setActive(request.isActive());
        a.setCreatedAt(LocalDateTime.now());
        a.setUpdatedAt(LocalDateTime.now());

        return AnnouncementResponse.from(announcementRepository.save(a));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listForVendor(String vendorEmail) {
        Account vendor = accountRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new IllegalStateException("Vendor not found: " + vendorEmail));
        return announcementRepository.findByVendorIdOrderBySortOrderAsc(vendor.getId())
                .stream()
                .map(AnnouncementResponse::from)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public AnnouncementResponse update(String vendorEmail, UUID id, UpdateAnnouncementRequest request) {
        Announcement a = findOwned(vendorEmail, id);
        a.setContentType(request.getContentType());
        a.setTextContent(request.getTextContent());
        a.setImageUrl(request.getImageUrl());
        a.setImageOrientation(request.getImageOrientation());
        a.setRedirectUrl(request.getRedirectUrl());
        a.setSortOrder(request.getSortOrder());
        a.setActive(request.isActive());
        a.setUpdatedAt(LocalDateTime.now());
        return AnnouncementResponse.from(announcementRepository.save(a));
    }

    /** {@inheritDoc} */
    @Override
    public void delete(String vendorEmail, UUID id) {
        Announcement a = findOwned(vendorEmail, id);
        announcementRepository.delete(a);
    }

    /** {@inheritDoc} */
    @Override
    public AnnouncementResponse moveUp(String vendorEmail, UUID id) {
        Account vendor = accountRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new IllegalStateException("Vendor not found: " + vendorEmail));
        List<Announcement> list = announcementRepository.findByVendorIdOrderBySortOrderAsc(vendor.getId());
        int idx = indexIn(list, id);
        if (idx > 0) {
            swapSortOrder(list.get(idx), list.get(idx - 1));
        }
        return AnnouncementResponse.from(list.get(idx));
    }

    /** {@inheritDoc} */
    @Override
    public AnnouncementResponse moveDown(String vendorEmail, UUID id) {
        Account vendor = accountRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new IllegalStateException("Vendor not found: " + vendorEmail));
        List<Announcement> list = announcementRepository.findByVendorIdOrderBySortOrderAsc(vendor.getId());
        int idx = indexIn(list, id);
        if (idx < list.size() - 1) {
            swapSortOrder(list.get(idx), list.get(idx + 1));
        }
        return AnnouncementResponse.from(list.get(idx));
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listActive() {
        return announcementRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(AnnouncementResponse::from)
                .toList();
    }

    // --- helpers ---

    /**
     * Loads an announcement by id and verifies it belongs to the given vendor.
     *
     * @param vendorEmail the authenticated vendor's email
     * @param id          the announcement UUID
     * @return the announcement entity
     * @throws AnnouncementNotFoundException if not found or owned by another vendor
     */
    private Announcement findOwned(String vendorEmail, UUID id) {
        Announcement a = announcementRepository.findById(id)
                .orElseThrow(() -> new AnnouncementNotFoundException(id));
        if (!a.getVendor().getEmail().equals(vendorEmail)) {
            throw new AnnouncementNotFoundException(id);
        }
        return a;
    }

    /**
     * Returns the list index of the announcement with the given id.
     *
     * @param list the ordered list of vendor announcements
     * @param id   the target announcement UUID
     * @return the index of the announcement in the list
     * @throws AnnouncementNotFoundException if the id is not present in the list
     */
    private int indexIn(List<Announcement> list, UUID id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(id)) return i;
        }
        throw new AnnouncementNotFoundException(id);
    }

    /**
     * Swaps the sort_order values of two adjacent announcements and saves both.
     *
     * @param a the announcement to move up
     * @param b the announcement to move down
     */
    private void swapSortOrder(Announcement a, Announcement b) {
        int tmp = a.getSortOrder();
        a.setSortOrder(b.getSortOrder());
        b.setSortOrder(tmp);
        announcementRepository.save(a);
        announcementRepository.save(b);
    }
}
