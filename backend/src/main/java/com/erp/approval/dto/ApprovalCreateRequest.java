package com.erp.approval.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.List;

@Getter
public class ApprovalCreateRequest {
    @NotBlank private String title;
    private String content;
    private String refType;
    private Long refId;
    @NotEmpty private List<Long> approverIds;
}
