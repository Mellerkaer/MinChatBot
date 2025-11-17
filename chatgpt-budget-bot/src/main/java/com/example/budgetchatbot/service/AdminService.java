package com.example.budgetchatbot.service;

import com.example.budgetchatbot.model.BudgetAdvice;
import com.example.budgetchatbot.repository.BudgetAdviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final BudgetAdviceRepository adviceRepository;

    public AdminService(BudgetAdviceRepository adviceRepository) {
        this.adviceRepository = adviceRepository;
    }

    public List<BudgetAdvice> getLatestBudgetAdvice() {
        return adviceRepository.findTop50ByOrderByCreatedAtDesc();
    }
}