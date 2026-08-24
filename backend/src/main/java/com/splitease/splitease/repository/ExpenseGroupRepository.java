package com.splitease.splitease.repository;

import com.splitease.splitease.model.ExpenseGroup;
import com.splitease.splitease.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseGroupRepository extends JpaRepository<ExpenseGroup, Long> {

    List<ExpenseGroup> findByMembersContaining(User user);
}