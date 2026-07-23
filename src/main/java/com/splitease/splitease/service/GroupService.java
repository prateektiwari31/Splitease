package com.splitease.splitease.service;

import com.splitease.splitease.dto.CreateGroupRequest;
import com.splitease.splitease.dto.GroupResponse;
import com.splitease.splitease.model.ExpenseGroup;
import com.splitease.splitease.model.User;
import com.splitease.splitease.repository.ExpenseGroupRepository;
import com.splitease.splitease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final ExpenseGroupRepository expenseGroupRepository;
    private final UserRepository userRepository;

    public GroupResponse createGroup(CreateGroupRequest request) {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication.getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ExpenseGroup group = ExpenseGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        group.getMembers().add(currentUser);

        ExpenseGroup savedGroup = expenseGroupRepository.save(group);

        return GroupResponse.builder()
                .id(savedGroup.getId())
                .name(savedGroup.getName())
                .description(savedGroup.getDescription())
                .createdBy(savedGroup.getCreatedBy().getName())
                .memberCount(savedGroup.getMembers().size())
                .build();
    }

    public List<GroupResponse> getMyGroups() {

        User currentUser = getCurrentUser();

        List<ExpenseGroup> groups = expenseGroupRepository.findByMembersContaining(currentUser);

        return groups.stream()
                .map(group -> GroupResponse.builder()
                        .id(group.getId())
                        .name(group.getName())
                        .description(group.getDescription())
                        .createdBy(group.getCreatedBy().getName())
                        .memberCount(group.getMembers().size())
                        .build())
                .toList();
    }
    public GroupResponse getGroup(Long groupId) {

        User currentUser = getCurrentUser();
        ExpenseGroup group = expenseGroupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        if (!group.getMembers().contains(currentUser)) {
            throw new RuntimeException("You are not a member of this group");
        }
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdBy(group.getCreatedBy().getName())
                .memberCount(group.getMembers().size())
                .build();
    }
    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}