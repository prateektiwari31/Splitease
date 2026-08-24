package com.splitease.splitease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBalance {
    private Long userId;
    private String userName;
    private Double netBalance; // positive = paisa milna hai, negative = paisa dena hai
}