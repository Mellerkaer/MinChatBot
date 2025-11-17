package com.example.budgetchatbot.api;

import com.example.budgetchatbot.service.AdminAuthService;
import com.example.budgetchatbot.service.AdminService;
import com.example.budgetchatbot.model.BudgetAdvice;
import com.example.budgetchatbot.model.AdminLoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final AdminAuthService adminAuthService;

    public AdminController(AdminService adminService, AdminAuthService adminAuthService) {
        this.adminService = adminService;
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody AdminLoginRequest request) {
        if (!adminAuthService.isValidPassword(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin password");
        }

        return ResponseEntity.ok().build();
    }

    // 2) Beskyttet endpoint til at hente seneste forespørgsler
    @GetMapping("/budget")
    public List<BudgetAdvice> getLatestBudgetRequests(
            @RequestHeader(value = "X-Admin-Token", required = false) String token) {

        if (!adminAuthService.isValidToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin token");
        }

        return adminService.getLatestBudgetAdvice();
    }
}