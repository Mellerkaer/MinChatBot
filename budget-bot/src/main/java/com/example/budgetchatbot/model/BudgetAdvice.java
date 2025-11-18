package com.example.budgetchatbot.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "budget_advice")
public class BudgetAdvice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double monthlyIncome;
    private Double rent;
    private Double fixedCosts;
    private Double savingsGoal;

    private String studyLine;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "advice_text", columnDefinition = "TEXT")
    private String adviceText;

    private LocalDateTime createdAt = LocalDateTime.now();
}