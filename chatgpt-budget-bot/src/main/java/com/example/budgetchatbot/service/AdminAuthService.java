package com.example.budgetchatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdminAuthService {

    private final String adminToken;

    public AdminAuthService(@Value("${admin.token}") String adminToken) {
        this.adminToken = adminToken;
    }

    public boolean isValidPassword(String password) {
        return password != null && password.equals(adminToken);
    }

    public boolean isValidToken(String token) {
        return token != null && token.equals(adminToken);
    }
}