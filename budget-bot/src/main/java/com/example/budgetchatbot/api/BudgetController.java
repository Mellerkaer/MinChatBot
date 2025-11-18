package com.example.budgetchatbot.api;

import com.example.budgetchatbot.dtos.BudgetRequest;
import com.example.budgetchatbot.dtos.AiResponse;
import com.example.budgetchatbot.model.BudgetAdvice;
import com.example.budgetchatbot.repository.BudgetAdviceRepository;
import com.example.budgetchatbot.service.OpenAiService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class BudgetController {

    private final OpenAiService openAiService;
    private final BudgetAdviceRepository adviceRepository;

    public BudgetController(OpenAiService openAiService,
                            BudgetAdviceRepository adviceRepository) {
        this.openAiService = openAiService;
        this.adviceRepository = adviceRepository;
    }

    @PostMapping("/budget")
    public AiResponse getBudget(@Valid @RequestBody BudgetRequest request) {
        AiResponse resp = openAiService.getBudgetTips(request);

        BudgetAdvice entity = new BudgetAdvice();
        entity.setMonthlyIncome(request.getMonthlyIncome());
        entity.setRent(request.getRent());
        entity.setFixedCosts(request.getFixedCosts());
        entity.setSavingsGoal(request.getSavingsGoal());
        entity.setStudyLine(request.getStudyLine());
        entity.setComment(request.getComment());
        entity.setAdviceText(resp.getAnswer());
        adviceRepository.save(entity);

        return resp;
    }
}
