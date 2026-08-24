package com.splitease.splitease.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExpenseResponse {

    private Long id;

    private String description;

    private Double totalAmount;

    private String paidBy;

    private String splitType;

    private LocalDateTime createdAt;

    private List<SplitInfo> splits;
}