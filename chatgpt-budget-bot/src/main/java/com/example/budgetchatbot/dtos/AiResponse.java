package com.example.budgetchatbot.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse {

    private String answer;
    private String statsText;

    public AiResponse(String answer) {
        this.answer = answer;
    }
}