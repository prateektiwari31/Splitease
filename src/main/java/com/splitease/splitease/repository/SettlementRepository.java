package com.splitease.splitease.repository;

import com.splitease.splitease.model.Expense;
import com.splitease.splitease.model.Settlement;
import com.splitease.splitease.model.SettlementStatus;
import com.splitease.splitease.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    List<Settlement> findByPayer(User payer);

    List<Settlement> findByReceiver(User receiver);

    List<Settlement> findByExpense(Expense expense);

    List<Settlement> findByStatus(SettlementStatus status);

    List<Settlement> findByPayerOrReceiver(User payer, User receiver);
}