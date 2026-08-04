package com.splitease.splitease.service;

import com.splitease.splitease.dto.AddMemberRequest;
import com.splitease.splitease.dto.CreateGroupRequest;
import com.splitease.splitease.dto.GroupResponse;
import com.splitease.splitease.dto.UserSearchResponse;
import com.splitease.splitease.exception.ApiException;
import com.splitease.splitease.model.ExpenseGroup;
import com.splitease.splitease.model.User;
import com.splitease.splitease.repository.ExpenseGroupRepository;
import com.splitease.splitease.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

        User currentUser = getCurrentUser();

        ExpenseGroup group = ExpenseGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        group.getMembers().add(currentUser);

        ExpenseGroup savedGroup = expenseGroupRepository.save(group);

        return mapToResponse(savedGroup);
    }

    public List<GroupResponse> getMyGroups() {

        User currentUser = getCurrentUser();

        List<ExpenseGroup> groups = expenseGroupRepository.findByMembersContaining(currentUser);

        return groups.stream()
                .map(this::mapToResponse)
                .toList();
    }

    public GroupResponse getGroup(Long groupId) {

        User currentUser = getCurrentUser();

        ExpenseGroup group = expenseGroupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException("Group not found", HttpStatus.NOT_FOUND));

        if (!group.getMembers().contains(currentUser)) {
            throw new ApiException("You are not a member of this group", HttpStatus.FORBIDDEN);
        }

        return mapToResponse(group);
    }

    public GroupResponse addMember(Long groupId, AddMemberRequest request) {

        User currentUser = getCurrentUser();

        ExpenseGroup group = expenseGroupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException("Group not found", HttpStatus.NOT_FOUND));

        if (!group.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ApiException("Only group creator can add members", HttpStatus.FORBIDDEN);
        }

        User newMember = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (group.getMembers().contains(newMember)) {
            throw new ApiException("User is already a member", HttpStatus.CONFLICT);
        }

        group.getMembers().add(newMember);

        ExpenseGroup savedGroup = expenseGroupRepository.save(group);

        return mapToResponse(savedGroup);
    }

    public GroupResponse removeMember(Long groupId, Long userId) {

        User currentUser = getCurrentUser();

        ExpenseGroup group = expenseGroupRepository.findById(groupId)
                .orElseThrow(() -> new ApiException("Group not found", HttpStatus.NOT_FOUND));

        if (!group.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ApiException("Only group creator can remove members", HttpStatus.FORBIDDEN);
        }

        User member = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        if (!group.getMembers().contains(member)) {
            throw new ApiException("User is not a member of this group", HttpStatus.BAD_REQUEST);
        }

        if (group.getCreatedBy().getId().equals(member.getId())) {
            throw new ApiException("Group creator cannot be removed", HttpStatus.BAD_REQUEST);
        }

        group.getMembers().remove(member);

        ExpenseGroup savedGroup = expenseGroupRepository.save(group);

        return mapToResponse(savedGroup);
    }

    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    private GroupResponse mapToResponse(ExpenseGroup group) {
        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdBy(group.getCreatedBy().getName())
                .memberCount(group.getMembers().size())
                .build();
    }

    public List<UserSearchResponse> searchUsers(String keyword) {

        return userRepository
                .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword,
                        keyword
                )
                .stream()
                .map(user -> new UserSearchResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail()
                ))
                .toList();
    }
}