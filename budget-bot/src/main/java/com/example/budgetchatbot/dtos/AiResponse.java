package com.example.budgetchatbot.dtos;

public class AiResponse {

    private String answer;
    private String statsText;
    private StatResult stats; // NY

    public AiResponse() {
    }

    public AiResponse(String answer, String statsText, StatResult stats) {
        this.answer = answer;
        this.statsText = statsText;
        this.stats = stats;
    }

    public String getAnswer() {
        return answer;
    }

    public String getStatsText() {
        return statsText;
    }

    public StatResult getStats() {
        return stats;
    }
}