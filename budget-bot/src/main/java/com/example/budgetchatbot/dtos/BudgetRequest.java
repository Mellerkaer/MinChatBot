package com.example.budgetchatbot.dtos;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BudgetRequest {

    @NotNull
    @PositiveOrZero
    private Double monthlyIncome;

    @NotNull
    @PositiveOrZero
    private Double rent;

    @NotNull
    @PositiveOrZero
    private Double fixedCosts;

    @PositiveOrZero
    private Double savingsGoal;

    @Size(max = 100)
    private String studyLine;

    @Size(max = 1000)
    private String comment;

    @Min(16) @Max(99)
    private Integer age;

    @Pattern(regexp = "MALE|FEMALE|OTHER|UNKNOWN")
    private String gender;
}