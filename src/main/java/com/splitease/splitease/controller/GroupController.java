package com.splitease.splitease.controller;

import com.splitease.splitease.dto.CreateGroupRequest;
import com.splitease.splitease.dto.GroupResponse;
import com.splitease.splitease.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request) {

        return ResponseEntity.ok(groupService.createGroup(request));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getMyGroups() {
        return ResponseEntity.ok(groupService.getMyGroups());
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable Long groupId) {

        return ResponseEntity.ok(groupService.getGroup(groupId));
    }
}