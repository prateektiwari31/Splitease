package com.splitease.splitease.repository;

import com.splitease.splitease.model.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, Long> {
}