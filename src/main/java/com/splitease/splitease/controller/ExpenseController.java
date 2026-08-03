package com.splitease.splitease.controller;

import com.splitease.splitease.dto.CreateExpenseRequest;
import com.splitease.splitease.dto.ExpenseResponse;
import com.splitease.splitease.dto.SettleUpRequest;
import com.splitease.splitease.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PostMapping("/{groupId}/settle")
    public ResponseEntity<ExpenseResponse> settleUp(
            @PathVariable Long groupId,
            @Valid @RequestBody SettleUpRequest request){

        return ResponseEntity.ok(expenseService.settleUp(groupId, request));
    }
    @GetMapping("/{groupId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(
            @PathVariable Long groupId){

        return ResponseEntity.ok(
                expenseService.getGroupExpenses(groupId));
    }
    @GetMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponse> getExpense(
            @PathVariable Long expenseId){

        return ResponseEntity.ok(
                expenseService.getExpense(expenseId));
    }
    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable Long expenseId){

        expenseService.deleteExpense(expenseId);

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(@PathVariable Long expenseId, @Valid @RequestBody CreateExpenseRequest request) {

        return ResponseEntity.ok(
                expenseService.updateExpense(expenseId, request));
    }
}
