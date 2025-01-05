package com.apitester.ttalkkag.dto;

import lombok.Data;

@Data
public class ItemOrderUpdateRequest {
    private Long draggedItemId;
    private Long targetParentId;
    private Integer targetOrder;
}
