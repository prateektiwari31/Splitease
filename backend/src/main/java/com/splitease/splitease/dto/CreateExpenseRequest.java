package com.splitease.splitease.dto;

import com.splitease.splitease.model.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateExpenseRequest {

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin(value = "0.01")
    private Double totalAmount;

    @NotNull
    private Long paidByUserId;

    @NotNull
    private SplitType splitType;

    private List<Long> participants;

    @Valid
    private List<ExpenseSplitRequest> splits;
}