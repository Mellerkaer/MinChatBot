package com.example.budgetchatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class StatService {

    private static final Logger log = LoggerFactory.getLogger(StatService.class);

    private final WebClient statClient;

    public StatService(WebClient.Builder builder) {
        this.statClient = builder
                .baseUrl("https://api.statbank.dk/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }


     // Tekst om, hvor personens indkomst ligger ift. andre unge,
     // baseret på medianindkomst fra Danmarks Statistik (INDKP201).
    public String getIncomePosition(Double monthlyIncome, Integer age, String gender) {
        if (monthlyIncome == null) {
            return "Jeg kender ikke din indkomst, så jeg kan ikke sammenligne den med andre unge.";
        }

        int ageVal = (age != null ? age : 22); // fallback: typisk studerende
        String ageGroupCode = mapAgeToStatbankAgeGroup(ageVal);
        String genderCode = mapGenderToStatbankCode(gender);

        Double medianFromDst = fetchMedianIncomeFromDst(ageGroupCode, genderCode);

        // Fallback hvis kaldet fejler
        if (medianFromDst == null) {
            log.warn("Faldt tilbage til simpel median-fallback for indkomstvurdering.");
            medianFromDst = 150_000.0; // ca. 150k om året
        }

        double yearlyIncome = monthlyIncome * 12.0;
        double ratio = yearlyIncome / medianFromDst;

        if (ratio < 0.8) {
            return "Din indkomst ligger under de fleste i din aldersgruppe i Danmark.";
        } else if (ratio <= 1.2) {
            return "Din indkomst ligger cirka som gennemsnittet for din aldersgruppe i Danmark.";
        } else {
            return "Din indkomst ligger over gennemsnittet for din aldersgruppe i Danmark.";
        }
    }

     // Mapper alder (år) til Statbank-alderskode (INDKP201).
     // Koderne er f.eks. "20-24", "25-29" osv.
    private String mapAgeToStatbankAgeGroup(int age) {
        if (age < 20) return "15-19";
        if (age <= 24) return "20-24";
        if (age <= 29) return "25-29";
        if (age <= 34) return "30-34";
        if (age <= 39) return "35-39";
        if (age <= 44) return "40-44";
        if (age <= 49) return "45-49";
        if (age <= 54) return "50-54";
        if (age <= 59) return "55-59";
        if (age <= 64) return "60-64";
        if (age <= 69) return "65-69";
        if (age <= 74) return "70-74";
        if (age <= 79) return "75-79";
        return "80+"; // du kan justere til den præcise kode i INDKP201
    }

     // Mapper køn indtastet i frontend til Statbank-kode.
     // "M" -> mænd, "K" -> kvinder, andet/null -> total.
    private String mapGenderToStatbankCode(String gender) {
        if (gender == null) return "TOT";
        String g = gender.trim().toUpperCase();
        return switch (g) {
            case "M" -> "M";   // mænd
            case "K" -> "K";   // kvinder
            default -> "TOT";  // total
        };
    }

     // Kalder INDKP201 og forsøger at hente medianindkomst (kr.) for en given aldersgruppe + køn.
     // Vi beder om:
     // - Indkomsttype: "2" = Indkomst i alt, før skatter mv. (fx)
     // - Enhed: "MEDIAN" (kode kan variere – tjek tabellen hvis det fejler)
     // - Population: "ALLE" (eller tilsvarende kode)
     // Hvis noget fejler, returneres null og fallback bruges.
    private Double fetchMedianIncomeFromDst(String ageGroupCode, String genderCode) {
        try {
            // Strukturen her viser bare MÅDEN at kalde på.
            Map<String, Object> body = Map.of(
                    "table", "INDKP201",
                    "time", List.of("2024"),          // seneste år
                    "INDKOMSTTYPE", List.of("2"),     // "Indkomst i alt før skatter mv."
                    "KØN", List.of(genderCode),
                    "ALDER", List.of(ageGroupCode),
                    "POPULATION", List.of("ALLE"),    // evt. "TOT" el. lign – tjek tabel
                    "PRISENHED", List.of("NP"),       // nominelle priser (kode kan variere)
                    "ENHED", List.of("MEDIAN")        // "Median (kr.)" – brug den rigtige kode
            );

            JsonNode root = statClient.post()
                    .uri("/data/INDKP201/JSONSTAT")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            log.debug("Svar fra Danmarks Statistik (INDKP201): {}", root);

            if (root == null) return null;

            JsonNode values = root.path("value");
            if (values.isArray() && values.size() > 0) {
                return values.get(0).asDouble();
            }

            return null;
        } catch (Exception e) {
            log.warn("Kunne ikke hente medianindkomst fra Danmarks Statistik (INDKP201)", e);
            return null;
        }
    }
}