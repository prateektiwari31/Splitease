package com.splitease.splitease.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrentUserResponse {

    private Long id;
    private String name;
    private String email;
}