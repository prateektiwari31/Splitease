package com.splitease.splitease.controller;

import com.splitease.splitease.dto.BalanceResponse;
import com.splitease.splitease.dto.SimplifiedDebt;
import com.splitease.splitease.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;

    @GetMapping("/{groupId}/balances")
    public ResponseEntity<BalanceResponse> getBalances(@PathVariable Long groupId) {
        return ResponseEntity.ok(balanceService.getGroupBalances(groupId));
    }

    @GetMapping("/{groupId}/simplify-debts")
    public ResponseEntity<List<SimplifiedDebt>> simplifyDebts(@PathVariable Long groupId) {
        return ResponseEntity.ok(balanceService.simplifyDebts(groupId));
    }
}