package com.erp.approval.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_line")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalLine {

    public enum LineStatus { PENDING, APPROVED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approval_request_id", nullable = false)
    private ApprovalRequest approvalRequest;

    @Column(nullable = false)
    private int sequence;

    @Column(nullable = false)
    private Long approverId;

    @Column(nullable = false, length = 100)
    private String approverName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private LineStatus status = LineStatus.PENDING;

    @Column(length = 500)
    private String comment;

    private LocalDateTime processedAt;

    public void approve(String comment) {
        this.status = LineStatus.APPROVED;
        this.comment = comment;
        this.processedAt = LocalDateTime.now();
    }

    public void reject(String comment) {
        this.status = LineStatus.REJECTED;
        this.comment = comment;
        this.processedAt = LocalDateTime.now();
    }
}
