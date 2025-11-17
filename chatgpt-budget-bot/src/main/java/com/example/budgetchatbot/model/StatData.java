package com.example.budgetchatbot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StatData {

    private Double avgIncome;
    private Double avgRent;

    public StatData(Double avgIncome, Double avgRent) {
        this.avgIncome = avgIncome;
        this.avgRent = avgRent;
    }
}
