package com.splitease.splitease.dto;

import com.splitease.splitease.model.SettlementStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SettlementResponse {

    private Long id;

    private String payerName;

    private String receiverName;

    private String groupName;

    private Double amount;

    private SettlementStatus status;

    private LocalDateTime settledAt;
}