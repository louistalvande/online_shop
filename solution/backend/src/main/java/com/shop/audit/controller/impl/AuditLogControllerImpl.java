package com.shop.audit.controller.impl;

import com.shop.audit.controller.AuditLogController;
import com.shop.audit.dto.AuditLogPageResponse;
import com.shop.audit.entity.AuditEventType;
import com.shop.audit.service.AuditLogService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/** Default implementation of {@link AuditLogController}. */
@RestController
public class AuditLogControllerImpl implements AuditLogController {

    private final AuditLogService auditLogService;

    /** @param auditLogService the audit log business-logic service */
    public AuditLogControllerImpl(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<AuditLogPageResponse> queryLogs(
            AuditEventType eventType, String email, String ipAddress,
            Instant from, Instant to, int page, int size) {
        return ResponseEntity.ok(
                auditLogService.queryLogs(eventType, email, ipAddress, from, to, page, size));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<byte[]> exportCsv(
            AuditEventType eventType, String email, String ipAddress,
            Instant from, Instant to) {
        byte[] csv = auditLogService.exportCsv(eventType, email, ipAddress, from, to);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("audit-log.csv").build());
        return ResponseEntity.ok().headers(headers).body(csv);
    }
}
