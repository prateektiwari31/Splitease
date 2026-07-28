package com.splitease.splitease.service;

import com.splitease.splitease.dto.CreateExpenseRequest;
import com.splitease.splitease.dto.ExpenseResponse;
import com.splitease.splitease.dto.SplitInfo;
import com.splitease.splitease.model.*;
import com.splitease.splitease.repository.ExpenseGroupRepository;
import com.splitease.splitease.repository.ExpenseRepository;
import com.splitease.splitease.repository.ExpenseSplitRepository;
import com.splitease.splitease.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final ExpenseGroupRepository expenseGroupRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public ExpenseResponse addExpense(Long groupId, CreateExpenseRequest request) {

        User currentUser = getCurrentUser();

        ExpenseGroup group = expenseGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!group.getMembers().contains(currentUser)) {
            throw new RuntimeException("You are not a member of this group");
        }

        User paidBy = userRepository.findById(request.getPaidByUserId())
                .orElseThrow(() -> new RuntimeException("Payer not found"));

        if (!group.getMembers().contains(paidBy)) {
            throw new RuntimeException("Payer must be a group member");
        }

        Expense expense = Expense.builder()
                .description(request.getDescription())
                .totalAmount(request.getTotalAmount())
                .paidBy(paidBy)
                .group(group)
                .splitType(request.getSplitType())
                .createdAt(LocalDateTime.now())
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        List<ExpenseSplit> expenseSplits = new ArrayList<>();

        switch (request.getSplitType()) {

            case EQUAL -> {

                if (request.getParticipants() == null || request.getParticipants().isEmpty()) {
                    throw new RuntimeException("Participants are required");
                }

                double amountPerPerson =
                        request.getTotalAmount() / request.getParticipants().size();

                for (Long userId : request.getParticipants()) {

                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    if (!group.getMembers().contains(user)) {
                        throw new RuntimeException(user.getName() + " is not a member of this group");
                    }

                    ExpenseSplit split = ExpenseSplit.builder()
                            .expense(savedExpense)
                            .user(user)
                            .amountOwed(amountPerPerson)
                            .build();

                    expenseSplits.add(split);
                }
            }

            case EXACT -> {

                if (request.getSplits() == null || request.getSplits().isEmpty()) {
                    throw new RuntimeException("Splits are required");
                }

                double total = 0;

                List<Long> addedUsers = new ArrayList<>();

                for (var splitRequest : request.getSplits()) {

                    User user = userRepository.findById(splitRequest.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    if (!group.getMembers().contains(user)) {
                        throw new RuntimeException(user.getName() + " is not a member of this group");
                    }

                    if (addedUsers.contains(user.getId())) {
                        throw new RuntimeException("Duplicate participant found");
                    }

                    addedUsers.add(user.getId());

                    total += splitRequest.getAmount();

                    ExpenseSplit split = ExpenseSplit.builder()
                            .expense(savedExpense)
                            .user(user)
                            .amountOwed(splitRequest.getAmount())
                            .build();

                    expenseSplits.add(split);
                }

                if (Math.abs(total - request.getTotalAmount()) > 0.01) {
                    throw new RuntimeException("Split amount does not match total amount");
                }
            }
            case PERCENTAGE -> {

                if (request.getSplits() == null || request.getSplits().isEmpty()) {
                    throw new RuntimeException("Splits are required");
                }

                double totalPercentage = 0;
                List<Long> addedUsers = new ArrayList<>();

                for (var splitRequest : request.getSplits()) {

                    User user = userRepository.findById(splitRequest.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    if (!group.getMembers().contains(user)) {
                        throw new RuntimeException(user.getName() + " is not a member of this group");
                    }

                    if (addedUsers.contains(user.getId())) {
                        throw new RuntimeException("Duplicate participant found");
                    }
                    addedUsers.add(user.getId());

                    totalPercentage += splitRequest.getPercentage();

                    double amountOwed = (splitRequest.getPercentage() / 100.0) * request.getTotalAmount();

                    ExpenseSplit split = ExpenseSplit.builder()
                            .expense(savedExpense)
                            .user(user)
                            .amountOwed(amountOwed)
                            .build();

                    expenseSplits.add(split);
                }

                if (Math.abs(totalPercentage - 100.0) > 0.01) {
                    throw new RuntimeException("Percentages must add up to 100");
                }
            }

            default ->
                    throw new RuntimeException(request.getSplitType() + " not supported yet");
        }

        expenseSplitRepository.saveAll(expenseSplits);

        List<SplitInfo> splitInfos = expenseSplits.stream()
                .map(s -> SplitInfo.builder()
                        .userName(s.getUser().getName())
                        .amountOwed(s.getAmountOwed())
                        .build())
                .toList();

        return ExpenseResponse.builder()
                .id(savedExpense.getId())
                .description(savedExpense.getDescription())
                .totalAmount(savedExpense.getTotalAmount())
                .paidBy(savedExpense.getPaidBy().getName())
                .splitType(savedExpense.getSplitType().name())
                .createdAt(savedExpense.getCreatedAt())
                .splits(splitInfos)
                .build();
    }

}