package com.splitease.splitease.controller;

import com.splitease.splitease.dto.CreateExpenseRequest;
import com.splitease.splitease.dto.ExpenseResponse;
import com.splitease.splitease.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/{groupId}/expenses")
    public ResponseEntity<ExpenseResponse> addExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateExpenseRequest request) {

        return ResponseEntity.ok(expenseService.addExpense(groupId, request));
    }
}