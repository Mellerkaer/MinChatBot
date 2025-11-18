package com.example.budgetchatbot.dtos;

public class StatResult {

    private Double userYearlyIncome;
    private Double medianYearlyIncome;
    private Double ratio;
    private String text; // den danske forklaring

    public StatResult() {
    }

    public StatResult(Double userYearlyIncome,
                      Double medianYearlyIncome,
                      Double ratio,
                      String text) {
        this.userYearlyIncome = userYearlyIncome;
        this.medianYearlyIncome = medianYearlyIncome;
        this.ratio = ratio;
        this.text = text;
    }

    public Double getUserYearlyIncome() {
        return userYearlyIncome;
    }

    public void setUserYearlyIncome(Double userYearlyIncome) {
        this.userYearlyIncome = userYearlyIncome;
    }

    public Double getMedianYearlyIncome() {
        return medianYearlyIncome;
    }

    public void setMedianYearlyIncome(Double medianYearlyIncome) {
        this.medianYearlyIncome = medianYearlyIncome;
    }

    public Double getRatio() {
        return ratio;
    }

    public void setRatio(Double ratio) {
        this.ratio = ratio;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}