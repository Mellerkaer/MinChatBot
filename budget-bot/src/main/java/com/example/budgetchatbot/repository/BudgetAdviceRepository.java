package com.example.budgetchatbot.repository;

import com.example.budgetchatbot.model.BudgetAdvice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetAdviceRepository extends JpaRepository<BudgetAdvice, Long> {

    List<BudgetAdvice> findTop50ByOrderByCreatedAtDesc();
}
