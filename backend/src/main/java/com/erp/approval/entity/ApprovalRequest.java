package com.erp.approval.entity;

import com.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "approval_request", indexes = {
        @Index(name = "idx_apr_company", columnList = "companyId"),
        @Index(name = "idx_apr_requester", columnList = "companyId, requesterId")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest extends BaseEntity {

    public enum ApprovalStatus { DRAFT, PENDING, APPROVED, REJECTED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 30)
    private String requestNo;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 2000)
    private String content;

    @Column(length = 50)
    private String refType;

    private Long refId;

    @Column(nullable = false)
    private Long requesterId;

    @Column(nullable = false, length = 100)
    private String requesterName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.DRAFT;

    @OneToMany(mappedBy = "approvalRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApprovalLine> lines = new ArrayList<>();

    public void submit() {
        if (status != ApprovalStatus.DRAFT) throw new IllegalStateException("기안 상태에서만 상신 가능합니다.");
        this.status = ApprovalStatus.PENDING;
    }

    public void approve() {
        this.status = ApprovalStatus.APPROVED;
    }

    public void reject() {
        this.status = ApprovalStatus.REJECTED;
    }

    public void cancel() {
        if (status == ApprovalStatus.APPROVED || status == ApprovalStatus.REJECTED)
            throw new IllegalStateException("완료된 결재는 취소할 수 없습니다.");
        this.status = ApprovalStatus.CANCELLED;
    }
}
