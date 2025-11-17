package com.example.budgetchatbot.dtos;

import lombok.Data;

@Data
public class BudgetRequest {

    private Double monthlyIncome;
    private Double rent;
    private Double fixedCosts;
    private Double savingsGoal;
    private String studyLine;
    private String comment;
    private Integer age;
    private String gender;
}