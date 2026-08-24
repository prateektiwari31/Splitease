package com.splitease.splitease.repository;

import com.splitease.splitease.model.Expense;
import com.splitease.splitease.model.ExpenseGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByGroupOrderByCreatedAtDesc(ExpenseGroup group);
}