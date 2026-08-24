package com.splitease.splitease.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupResponse {

    private Long id;

    private String name;

    private String description;

    private String createdBy;

    private Integer memberCount;

}