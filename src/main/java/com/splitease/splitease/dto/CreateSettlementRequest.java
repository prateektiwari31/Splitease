package com.splitease.splitease.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSettlementRequest {

    @NotNull
    private Long receiverId;

    @NotNull
    private Long groupId;

    @NotNull
    private Double amount;
}