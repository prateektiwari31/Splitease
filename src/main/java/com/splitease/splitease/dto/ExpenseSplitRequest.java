package com.splitease.splitease.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExpenseSplitRequest {

    @NotNull
    private Long userId;

    @DecimalMin(value = "0.0", inclusive = false)
    private Double amount;

    @DecimalMin(value = "0.0", inclusive = false)
    private Double percentage;
}