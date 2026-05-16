package com.erp.master.dto;

import com.erp.master.entity.Partner;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PartnerRequest {
    @NotBlank private String partnerName;
    private String businessNo;
    private Partner.PartnerType partnerType;
    private String representativeName;
    private String address;
    private String phone;
    private String email;
}
