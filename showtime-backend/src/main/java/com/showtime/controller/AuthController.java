package com.showtime.controller;

import com.showtime.dto.LoginRequest;
import com.showtime.dto.RegisterRequest;
import com.showtime.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest r) {
        return auth.register(r);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest r) {
        return auth.login(r);
    }

    @PostMapping("/logout")
    public Map<String, String> logout(@RequestHeader("Authorization") String header) {
        auth.logout(header.replace("Bearer ", "").trim());
        return Map.of("status", "logged out");
    }
}
