package com.erp.common.audit;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository repository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long companyId, String entityType, Long entityId, String action,
                    String oldValue, String newValue, String performedBy) {
        repository.save(AuditLog.builder()
                .companyId(companyId)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .build());
    }
}

@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_entity", columnList = "entityType, entityId"),
        @Index(name = "idx_audit_company", columnList = "companyId")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 50)
    private String entityType;

    private Long entityId;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(columnDefinition = "TEXT")
    private String oldValue;

    @Column(columnDefinition = "TEXT")
    private String newValue;

    @Column(length = 100)
    private String performedBy;

    private LocalDateTime performedAt;
}

interface AuditLogRepository extends org.springframework.data.jpa.repository.JpaRepository<AuditLog, Long> {
}
