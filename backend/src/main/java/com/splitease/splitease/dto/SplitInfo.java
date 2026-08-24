package com.splitease.splitease.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SplitInfo {
    private String userName;
    private Double amountOwed;
}