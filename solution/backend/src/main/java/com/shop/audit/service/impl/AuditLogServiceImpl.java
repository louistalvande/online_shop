package com.shop.audit.service.impl;

import com.shop.audit.entity.AuditEventType;
import com.shop.audit.entity.AuditLog;
import com.shop.audit.repository.AuditLogRepository;
import com.shop.audit.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Persists audit entries and resolves the originating IP from the current request context (CPA-13).
 * Extracts the real client IP by checking the {@code X-Forwarded-For} header first
 * (set by Nginx), then falling back to {@code remoteAddr}.
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Constructs the service with the audit log repository.
     *
     * @param auditLogRepository the JPA repository for audit records
     */
    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /** {@inheritDoc} */
    @Override
    public void log(AuditEventType eventType, String email, String details) {
        AuditLog entry = new AuditLog();
        entry.setEventType(eventType);
        entry.setEmail(email);
        entry.setIpAddress(resolveClientIp());
        entry.setDetails(details);
        auditLogRepository.save(entry);
    }

    /**
     * Resolves the real client IP from the current request.
     * Reads {@code X-Forwarded-For} first (Nginx proxy header), then falls back to
     * {@code remoteAddr}. Returns {@code null} when called outside a request context.
     *
     * @return the client IP address, or {@code null}
     */
    private String resolveClientIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
