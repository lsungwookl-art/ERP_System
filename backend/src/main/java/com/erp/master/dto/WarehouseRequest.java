package com.erp.master.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class WarehouseRequest {
    @NotBlank private String warehouseName;
    private String address;
    private String description;
}
