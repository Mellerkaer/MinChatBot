// java
package com.example.budgetchatbot.service;

import com.example.budgetchatbot.dtos.AiResponse;
import com.example.budgetchatbot.dtos.StatResult;
import com.example.budgetchatbot.dtos.BudgetRequest;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class OpenAiService {

    private static final Logger logger = LoggerFactory.getLogger(OpenAiService.class);

    private final WebClient webClient;
    private final String model;
    private final int maxOutputTokens;
    private final StatService statService;

    public OpenAiService(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.api.url}") String baseUrl,
            @Value("${openai.model}") String model,
            @Value("${openai.maxOutputTokens:600}") int maxOutputTokens,
            StatService statService
    ) {
        this.model = model;
        this.maxOutputTokens = maxOutputTokens;
        this.statService = statService;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public AiResponse getBudgetTips(BudgetRequest req) {

        // Hent både tekst og tal
        StatResult stat = statService.getIncomeStats(req.getMonthlyIncome());
        String incomePositionText =
                (stat != null ? stat.getText()
                        : "Jeg kunne ikke hente tal fra Danmarks Statistik lige nu.");

        String userPrompt = buildUserPrompt(req, incomePositionText);

        // Minimal request body for Responses API
        Map<String, Object> body = Map.of(
                "model", model,
                "input", List.of(Map.of("role", "user", "content", userPrompt)),
                "max_output_tokens", maxOutputTokens
        );

        try {
            JsonNode response = webClient.post()
                    .uri("/responses")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            String text = extractText(response);
            if (text == null || text.isBlank()) {
                text = "Jeg kunne desværre ikke generere et svar lige nu. Prøv igen om lidt.";
            }

            return new AiResponse(text, incomePositionText, stat);

        } catch (Exception e) {
            logger.error("Fejl ved kald til OpenAI", e);
            return new AiResponse(
                    "Der opstod en fejl ved kald til OpenAI. Tjek backend-loggen mens du udvikler.",
                    null,
                    null
            );
        }
    }

    private String buildUserPrompt(BudgetRequest r, String incomePositionText) {

        return """
            Jeg er studerende og vil gerne have hjælp til mit budget.

            Månedlig indkomst: %s kr
            Husleje: %s kr
            Øvrige faste udgifter: %s kr
            Ønsket opsparing pr. måned: %s kr
            Studieretning: %s
            Ekstra info: %s

            Sammenligning med andre unge i Danmark (baseret på tal fra Danmarks Statistik / estimat):
            %s

            Lav:
            1) Et realistisk, overskueligt budgetforslag (beløbene må gerne være intervaller).
            2) 3–7 konkrete spare-tips, der passer til min situation.
            3) En kort opsummering på maks. 3 linjer.
            """
                .formatted(
                        formatDouble(r.getMonthlyIncome()),
                        formatDouble(r.getRent()),
                        formatDouble(r.getFixedCosts()),
                        formatDouble(r.getSavingsGoal()),
                        emptyIfNull(r.getStudyLine()),
                        emptyIfNull(r.getComment()),
                        incomePositionText
                );
    }

    private String formatDouble(Double d) {
        if (d == null) return "ukendt";
        return String.format(Locale.GERMANY, "%.0f", d);
    }

    private String emptyIfNull(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private String extractText(JsonNode root) {
        if (root == null) return null;
        StringBuilder sb = new StringBuilder();
        for (JsonNode outputNode : root.path("output")) {
            for (JsonNode contentNode : outputNode.path("content")) {
                if ("output_text".equals(contentNode.path("type").asText())) {
                    String chunk = contentNode.path("text").asText();
                    sb.append(chunk);
                }
            }
        }
        return sb.toString();
    }
}
