package com.example.budgetchatbot.service;

import com.example.budgetchatbot.dtos.StatResult;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class StatService {

    private static final Logger log = Logger.getLogger(StatService.class.getName());

    private final WebClient statClient;

    public StatService(WebClient.Builder builder) {
        this.statClient = builder
                .baseUrl("https://api.statbank.dk/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * Bruges af backend/JS til grafen.
     * Returnerer både:
     *  - brugerens årsindkomst
     *  - medianårsindkomst fra INDKP201
     *  - ratio (bruger / median)
     *  - dansk forklaringstekst
     */
    public StatResult getIncomeStats(Double monthlyIncome) {
        if (monthlyIncome == null) {
            return null;
        }

        double yearlyIncome = monthlyIncome * 12.0;

        // LIGE NU: alder 20-24 og alle køn ("MOK")
        Double medianFromDst = fetchMedianIncomeFromDst("20-24", "MOK");
        double median;

        if (medianFromDst != null) {
            median = medianFromDst;
        } else {
            // simpelt fallback
            median = 150_000.0;
        }

        double ratio = yearlyIncome / median;

        String text;
        if (ratio < 0.8) {
            text = "Din indkomst ligger under de fleste 20-24-årige i Danmark.";
        } else if (ratio <= 1.2) {
            text = "Din indkomst ligger cirka som gennemsnittet for 20-24-årige i Danmark.";
        } else {
            text = "Din indkomst ligger over gennemsnittet for 20-24-årige i Danmark.";
        }

        return new StatResult(yearlyIncome, median, ratio, text);
    }

    /**
     * Bruges i dag til at bygge forklaringstekst til prompten.
     * Den genbruger blot getIncomeStats().
     */
    public String getIncomePosition(Double monthlyIncome) {
        StatResult stats = getIncomeStats(monthlyIncome);
        if (stats == null) {
            return "Jeg kender ikke din indkomst, så jeg kan ikke sammenligne den med andre unge.";
        }
        return stats.getText();
    }

    // --------- Kald til INDKP201 ---------

    private Double fetchMedianIncomeFromDst(String ageGroupCode, String genderCode) {
        try {
            Map<String, Object> body = Map.of(
                    "table", "INDKP201",
                    "format", "JSONSTAT",
                    "variables", List.of(
                            Map.of("code", "INDKOMSTTYPE", "values", List.of("105")),      // Indkomst i alt, før skat
                            Map.of("code", "KOEN",         "values", List.of(genderCode)), // "MOK", "M" eller "K"
                            Map.of("code", "ALDER",        "values", List.of(ageGroupCode)),
                            Map.of("code", "POPU",         "values", List.of("5000")),     // alle uanset om de har typen
                            Map.of("code", "PRISENHED",    "values", List.of("006")),      // nominelle priser
                            Map.of("code", "ENHED",        "values", List.of("0055")),     // median (kr.)
                            Map.of("code", "Tid",          "values", List.of("2024"))      // seneste år
                    )
            );

            JsonNode root = statClient.post()
                    .uri("/data/INDKP201")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            log.fine("Svar fra Danmarks Statistik (INDKP201): " + root);

            if (root == null) return null;

            JsonNode values = root.path("dataset").path("value");
            if (values.isArray() && values.size() > 0 && !values.get(0).isNull()) {
                return values.get(0).asDouble();
            }
            return null;

        } catch (Exception e) {
            log.log(Level.WARNING,
                    "Kunne ikke hente medianindkomst fra Danmarks Statistik (INDKP201)", e);
            return null;
        }
    }
}