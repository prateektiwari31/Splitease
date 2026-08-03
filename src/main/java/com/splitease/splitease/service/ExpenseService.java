package com.splitease.splitease.service;

import com.splitease.splitease.dto.CreateExpenseRequest;
import com.splitease.splitease.dto.ExpenseResponse;
import com.splitease.splitease.dto.SettleUpRequest;
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
                .amount(request.getTotalAmount())
                .paidBy(paidBy)
                .group(group)
                .splitType(request.getSplitType())
                .createdAt(LocalDateTime.now())
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        List<ExpenseSplit> expenseSplits =
                createExpenseSplits(savedExpense, group, request);

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
                .totalAmount(savedExpense.getAmount())
                .paidBy(savedExpense.getPaidBy().getName())
                .splitType(savedExpense.getSplitType().name())
                .createdAt(savedExpense.getCreatedAt())
                .splits(splitInfos)
                .build();
    }
    public ExpenseResponse settleUp(Long groupId, SettleUpRequest request) {

        User currentUser = getCurrentUser();

        ExpenseGroup group = expenseGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!group.getMembers().contains(currentUser)) {
            throw new RuntimeException("You are not a member of this group");
        }

        User payer = userRepository.findById(request.getPayerId())
                .orElseThrow(() -> new RuntimeException("Payer not found"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (!group.getMembers().contains(payer)) {
            throw new RuntimeException("Payer is not a group member");
        }

        if (!group.getMembers().contains(receiver)) {
            throw new RuntimeException("Receiver is not a group member");
        }

        if (payer.getId().equals(receiver.getId())) {
            throw new RuntimeException("Payer and Receiver cannot be same");
        }

        Expense expense = Expense.builder()
                .description("Settlement")
                .amount(request.getAmount())
                .paidBy(payer)
                .group(group)
                .splitType(SplitType.EXACT)
                .createdAt(LocalDateTime.now())
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        ExpenseSplit split = ExpenseSplit.builder()
                .expense(savedExpense)
                .user(receiver)
                .amountOwed(request.getAmount())
                .build();

        expenseSplitRepository.save(split);

        return ExpenseResponse.builder()
                .id(savedExpense.getId())
                .description(savedExpense.getDescription())
                .totalAmount(savedExpense.getAmount())
                .paidBy(savedExpense.getPaidBy().getName())
                .splitType(savedExpense.getSplitType().name())
                .createdAt(savedExpense.getCreatedAt())
                .splits(List.of(
                        SplitInfo.builder()
                                .userName(receiver.getName())
                                .amountOwed(request.getAmount())
                                .build()
                ))
                .build();
    }

    public List<ExpenseResponse> getGroupExpenses(Long groupId) {

        User currentUser = getCurrentUser();

        ExpenseGroup group = expenseGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!group.getMembers().contains(currentUser)) {
            throw new RuntimeException("You are not a member of this group");
        }

        List<Expense> expenses = expenseRepository.findByGroupOrderByCreatedAtDesc(group);

        return expenses.stream().map(expense -> {

            List<ExpenseSplit> splits =
                    expenseSplitRepository.findByExpense(expense);

            List<SplitInfo> splitInfos = splits.stream()
                    .map(split -> SplitInfo.builder()
                            .userName(split.getUser().getName())
                            .amountOwed(split.getAmountOwed())
                            .build())
                    .toList();

            return ExpenseResponse.builder()
                    .id(expense.getId())
                    .description(expense.getDescription())
                    .totalAmount(expense.getAmount())
                    .paidBy(expense.getPaidBy().getName())
                    .splitType(expense.getSplitType().name())
                    .createdAt(expense.getCreatedAt())
                    .splits(splitInfos)
                    .build();

        }).toList();
    }

    public ExpenseResponse getExpense(Long expenseId){

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() ->
                        new RuntimeException("Expense not found"));

        List<ExpenseSplit> splits =
                expenseSplitRepository.findByExpense(expense);

        List<SplitInfo> splitInfos = splits.stream()
                .map(split -> SplitInfo.builder()
                        .userName(split.getUser().getName())
                        .amountOwed(split.getAmountOwed())
                        .build())
                .toList();

        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .totalAmount(expense.getAmount())
                .paidBy(expense.getPaidBy().getName())
                .splitType(expense.getSplitType().name())
                .createdAt(expense.getCreatedAt())
                .splits(splitInfos)
                .build();
    }

    public void deleteExpense(Long expenseId){

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() ->
                        new RuntimeException("Expense not found"));

        expenseSplitRepository.deleteAll(
                expenseSplitRepository.findByExpense(expense));

        expenseRepository.delete(expense);
    }


    public ExpenseResponse updateExpense(Long expenseId,
                                         CreateExpenseRequest request) {

        User currentUser = getCurrentUser();

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        ExpenseGroup group = expense.getGroup();

        if (!group.getMembers().contains(currentUser)) {
            throw new RuntimeException("You are not a member of this group");
        }

        User paidBy = userRepository.findById(request.getPaidByUserId())
                .orElseThrow(() -> new RuntimeException("Payer not found"));

        if (!group.getMembers().contains(paidBy)) {
            throw new RuntimeException("Payer must be a group member");
        }

        // Expense update
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getTotalAmount());
        expense.setPaidBy(paidBy);
        expense.setSplitType(request.getSplitType());

        expenseRepository.save(expense);

        // Purane splits delete
        List<ExpenseSplit> oldSplits =
                expenseSplitRepository.findByExpense(expense);

        expenseSplitRepository.deleteAll(oldSplits);

        // Naye splits generate
        List<ExpenseSplit> newSplits =
                createExpenseSplits(expense, group, request);

        expenseSplitRepository.saveAll(newSplits);

        List<SplitInfo> splitInfos = newSplits.stream()
                .map(s -> SplitInfo.builder()
                        .userName(s.getUser().getName())
                        .amountOwed(s.getAmountOwed())
                        .build())
                .toList();

        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .totalAmount(expense.getAmount())
                .paidBy(expense.getPaidBy().getName())
                .splitType(expense.getSplitType().name())
                .createdAt(expense.getCreatedAt())
                .splits(splitInfos)
                .build();
    }
    private List<ExpenseSplit> createExpenseSplits(
            Expense expense,
            ExpenseGroup group,
            CreateExpenseRequest request) {

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

                    expenseSplits.add(
                            ExpenseSplit.builder()
                                    .expense(expense)
                                    .user(user)
                                    .amountOwed(amountPerPerson)
                                    .build()
                    );
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

                    expenseSplits.add(
                            ExpenseSplit.builder()
                                    .expense(expense)
                                    .user(user)
                                    .amountOwed(splitRequest.getAmount())
                                    .build()
                    );
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

                    double amountOwed =
                            (splitRequest.getPercentage() / 100.0) * request.getTotalAmount();

                    expenseSplits.add(
                            ExpenseSplit.builder()
                                    .expense(expense)
                                    .user(user)
                                    .amountOwed(amountOwed)
                                    .build()
                    );
                }

                if (Math.abs(totalPercentage - 100.0) > 0.01) {
                    throw new RuntimeException("Percentages must add up to 100");
                }
            }

            default -> throw new RuntimeException("Split type not supported");
        }

        return expenseSplits;
    }
}