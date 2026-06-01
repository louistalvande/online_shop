package com.shop.audit.service.impl;

import com.shop.audit.dto.AuditLogPageResponse;
import com.shop.audit.dto.AuditLogResponse;
import com.shop.audit.entity.AuditEventType;
import com.shop.audit.entity.AuditLog;
import com.shop.audit.repository.AuditLogRepository;
import com.shop.audit.service.AuditLogService;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists and queries audit entries (US-SEC-05 / SEC-LOG-001 / CPA-13).
 * Resolves the originating IP from {@code X-Forwarded-For} (Nginx) then {@code remoteAddr}.
 */
@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final int MAX_PAGE_SIZE = 200;

    private final AuditLogRepository auditLogRepository;

    /**
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

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public AuditLogPageResponse queryLogs(AuditEventType eventType, String email, String ipAddress,
                                           Instant from, Instant to, int page, int size) {
        int clampedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        PageRequest pageable = PageRequest.of(page, clampedSize,
                Sort.by(Sort.Direction.DESC, "occurredAt"));

        Page<AuditLog> result = auditLogRepository.findAll(
                buildSpec(eventType, email, ipAddress, from, to), pageable);

        List<AuditLogResponse> content = result.getContent().stream()
                .map(AuditLogResponse::from)
                .toList();

        return new AuditLogPageResponse(content, result.getTotalElements(),
                result.getTotalPages(), page, clampedSize);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv(AuditEventType eventType, String email, String ipAddress,
                             Instant from, Instant to) {
        List<AuditLog> all = auditLogRepository.findAll(
                buildSpec(eventType, email, ipAddress, from, to),
                Sort.by(Sort.Direction.DESC, "occurredAt"));

        StringBuilder csv = new StringBuilder("id,eventType,email,ipAddress,details,occurredAt\n");
        for (AuditLog log : all) {
            csv.append(log.getId()).append(',')
               .append(escapeCsv(log.getEventType().name())).append(',')
               .append(escapeCsv(log.getEmail())).append(',')
               .append(escapeCsv(log.getIpAddress())).append(',')
               .append(escapeCsv(log.getDetails())).append(',')
               .append(log.getOccurredAt()).append('\n');
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Specification<AuditLog> buildSpec(AuditEventType eventType, String email,
                                               String ipAddress, Instant from, Instant to) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (eventType  != null) predicates.add(cb.equal(root.get("eventType"), eventType));
            if (email      != null && !email.isBlank())
                predicates.add(cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
            if (ipAddress  != null && !ipAddress.isBlank())
                predicates.add(cb.equal(root.get("ipAddress"), ipAddress));
            if (from       != null) predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
            if (to         != null) predicates.add(cb.lessThanOrEqualTo(root.get("occurredAt"), to));
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String resolveClientIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}
