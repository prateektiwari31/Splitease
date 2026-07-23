package com.splitease.splitease.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGroupRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 100)
    private String name;

    @Size(max = 255)
    private String description;

}