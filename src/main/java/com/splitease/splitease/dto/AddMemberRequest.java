package com.splitease.splitease.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddMemberRequest {

    @NotBlank
    @Email
    private String email;
}