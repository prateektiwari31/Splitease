package com.splitease.splitease.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSearchResponse {

    private Long id;
    private String name;
    private String email;
}