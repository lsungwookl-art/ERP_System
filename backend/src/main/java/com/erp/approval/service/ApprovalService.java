package com.erp.approval.service;

import com.erp.approval.dto.ApprovalCreateRequest;
import com.erp.approval.entity.ApprovalLine;
import com.erp.approval.entity.ApprovalRequest;
import com.erp.approval.repository.ApprovalRequestRepository;
import com.erp.common.exception.BusinessException;
import com.erp.common.service.DocSequenceService;
import com.erp.master.entity.User;
import com.erp.master.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalService {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final UserRepository userRepository;
    private final DocSequenceService docSequenceService;

    @Transactional(readOnly = true)
    public Page<ApprovalRequest> list(Long companyId, ApprovalRequest.ApprovalStatus status, Pageable pageable) {
        return approvalRequestRepository.findByFilter(companyId, status, pageable);
    }

    @Transactional
    public ApprovalRequest create(Long companyId, Long requesterId, ApprovalCreateRequest req) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> BusinessException.notFound("사용자"));

        Map<Long, User> approverMap = userRepository.findAllById(req.getApproverIds())
                .stream().collect(Collectors.toMap(User::getId, u -> u));

        String requestNo = docSequenceService.next(companyId, "APR");

        ApprovalRequest request = ApprovalRequest.builder()
                .companyId(companyId)
                .requestNo(requestNo)
                .title(req.getTitle())
                .content(req.getContent())
                .refType(req.getRefType())
                .refId(req.getRefId())
                .requesterId(requesterId)
                .requesterName(requester.getName())
                .build();

        for (int i = 0; i < req.getApproverIds().size(); i++) {
            Long approverId = req.getApproverIds().get(i);
            User approver = approverMap.get(approverId);
            if (approver == null) throw BusinessException.notFound("결재자(id=" + approverId + ")");

            request.getLines().add(ApprovalLine.builder()
                    .approvalRequest(request)
                    .sequence(i + 1)
                    .approverId(approverId)
                    .approverName(approver.getName())
                    .build());
        }

        request.submit();
        return approvalRequestRepository.save(request);
    }

    @Transactional
    public ApprovalRequest approve(Long companyId, Long requestId, Long approverId, String comment) {
        ApprovalRequest request = getRequest(companyId, requestId);
        if (request.getStatus() != ApprovalRequest.ApprovalStatus.PENDING) {
            throw new BusinessException("결재 대기 상태가 아닙니다.", "INVALID_STATUS", HttpStatus.BAD_REQUEST);
        }

        ApprovalLine line = request.getLines().stream()
                .filter(l -> l.getApproverId().equals(approverId) && l.getStatus() == ApprovalLine.LineStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new BusinessException("결재 권한이 없습니다.", "FORBIDDEN", HttpStatus.FORBIDDEN));

        line.approve(comment);
        boolean allApproved = request.getLines().stream()
                .allMatch(l -> l.getStatus() == ApprovalLine.LineStatus.APPROVED);
        if (allApproved) request.approve();

        return approvalRequestRepository.save(request);
    }

    @Transactional
    public ApprovalRequest reject(Long companyId, Long requestId, Long approverId, String comment) {
        ApprovalRequest request = getRequest(companyId, requestId);
        if (request.getStatus() != ApprovalRequest.ApprovalStatus.PENDING) {
            throw new BusinessException("결재 대기 상태가 아닙니다.", "INVALID_STATUS", HttpStatus.BAD_REQUEST);
        }

        ApprovalLine line = request.getLines().stream()
                .filter(l -> l.getApproverId().equals(approverId) && l.getStatus() == ApprovalLine.LineStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new BusinessException("결재 권한이 없습니다.", "FORBIDDEN", HttpStatus.FORBIDDEN));

        line.reject(comment);
        request.reject();
        return approvalRequestRepository.save(request);
    }

    private ApprovalRequest getRequest(Long companyId, Long requestId) {
        return approvalRequestRepository.findById(requestId)
                .filter(r -> r.getCompanyId().equals(companyId) && !r.isDeleted())
                .orElseThrow(() -> BusinessException.notFound("결재요청"));
    }
}
