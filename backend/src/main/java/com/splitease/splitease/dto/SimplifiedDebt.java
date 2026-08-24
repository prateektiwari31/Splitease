package com.splitease.splitease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimplifiedDebt {
    private String fromUser;   // jisko dena hai
    private String toUser;     // jisko milna hai
    private Double amount;
}