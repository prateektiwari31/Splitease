package com.splitease.splitease.service;

import com.splitease.splitease.dto.BalanceResponse;
import com.splitease.splitease.dto.SimplifiedDebt;
import com.splitease.splitease.dto.UserBalance;
import com.splitease.splitease.model.Expense;
import com.splitease.splitease.model.ExpenseGroup;
import com.splitease.splitease.model.ExpenseSplit;
import com.splitease.splitease.model.User;
import com.splitease.splitease.repository.ExpenseGroupRepository;
import com.splitease.splitease.repository.ExpenseRepository;
import com.splitease.splitease.repository.ExpenseSplitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final ExpenseGroupRepository expenseGroupRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;

    public BalanceResponse getGroupBalances(Long groupId) {

        ExpenseGroup group = expenseGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        Map<Long, Double> netBalanceMap = new HashMap<>();

        // Har user ko 0 se initialize karo
        for (User member : group.getMembers()) {
            netBalanceMap.put(member.getId(), 0.0);
        }

        // Step 1: Jisne expense pay kiya, uska balance badhao (+)
        List<Expense> expenses = expenseRepository.findByGroupOrderByCreatedAtDesc(group);
        for (Expense expense : expenses) {
            Long payerId = expense.getPaidBy().getId();
            netBalanceMap.merge(payerId, expense.getAmount(), Double::sum);
        }

        // Step 2: Jiska split hai, uska balance ghatao (-)
        for (Expense expense : expenses) {
            List<ExpenseSplit> splits = expenseSplitRepository.findByExpense(expense);
            for (ExpenseSplit split : splits) {
                Long userId = split.getUser().getId();
                netBalanceMap.merge(userId, -split.getAmountOwed(), Double::sum);
            }
        }

        List<UserBalance> balances = group.getMembers().stream()
                .map(member -> UserBalance.builder()
                        .userId(member.getId())
                        .userName(member.getName())
                        .netBalance(Math.round(netBalanceMap.get(member.getId()) * 100.0) / 100.0)
                        .build())
                .collect(Collectors.toList());

        return BalanceResponse.builder()
                .groupId(groupId)
                .balances(balances)
                .build();
    }

    public List<SimplifiedDebt> simplifyDebts(Long groupId) {

        BalanceResponse balanceResponse = getGroupBalances(groupId);

        // Step 1: Creditors (positive balance) aur Debtors (negative balance) alag karo
        List<UserBalance> creditors = new ArrayList<>();
        List<UserBalance> debtors = new ArrayList<>();

        for (UserBalance ub : balanceResponse.getBalances()) {
            if (ub.getNetBalance() > 0.01) {
                creditors.add(ub);
            } else if (ub.getNetBalance() < -0.01) {
                debtors.add(ub);
            }
        }

        // Step 2: Descending order mein sort karo (sabse zyada wale pehle)
        creditors.sort((a, b) -> Double.compare(b.getNetBalance(), a.getNetBalance()));
        debtors.sort((a, b) -> Double.compare(a.getNetBalance(), b.getNetBalance())); // most negative first

        List<SimplifiedDebt> transactions = new ArrayList<>();

        int i = 0, j = 0;

        // Step 3: Greedy matching — max creditor ko max debtor se match karo
        while (i < creditors.size() && j < debtors.size()) {

            UserBalance creditor = creditors.get(i);
            UserBalance debtor = debtors.get(j);

            double creditAmount = creditor.getNetBalance();
            double debtAmount = -debtor.getNetBalance(); // negative ko positive banaya

            double settledAmount = Math.min(creditAmount, debtAmount);

            transactions.add(SimplifiedDebt.builder()
                    .fromUser(debtor.getUserName())
                    .toUser(creditor.getUserName())
                    .amount(Math.round(settledAmount * 100.0) / 100.0)
                    .build());

            // Balances update karo
            creditor.setNetBalance(creditAmount - settledAmount);
            debtor.setNetBalance(debtor.getNetBalance() + settledAmount);

            // Jiska balance 0 ho gaya, usko aage badhao
            if (Math.abs(creditor.getNetBalance()) < 0.01) i++;
            if (Math.abs(debtor.getNetBalance()) < 0.01) j++;
        }

        return transactions;
    }
}