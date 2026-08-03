package com.splitease.splitease.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SettleUpRequest {

    @NotNull
    private Long payerId;

    @NotNull
    private Long receiverId;

    @NotNull
    private Double amount;
}