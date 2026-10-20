package com.showtime.service;

import com.showtime.dto.LoginRequest;
import com.showtime.dto.RegisterRequest;
import com.showtime.entity.User;
import com.showtime.exception.ApiException;
import com.showtime.repository.UserRepository;
import com.showtime.security.TokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final TokenService tokens;

    public AuthService(UserRepository users, PasswordEncoder encoder, TokenService tokens) {
        this.users = users;
        this.encoder = encoder;
        this.tokens = tokens;
    }

    public Map<String, Object> register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "This email is already registered. Please log in.");
        }
        User u = new User();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setCreatedAt(LocalDateTime.now(IST));
        users.save(u);
        return session(u);
    }

    public Map<String, Object> login(LoginRequest r) {
        User u = users.findByEmail(r.email().trim().toLowerCase()).orElse(null);
        if (u == null || !encoder.matches(r.password(), u.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return session(u);
    }

    public void logout(String token) {
        tokens.revoke(token);
    }

    private Map<String, Object> session(User u) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("token", tokens.issue(u.getId()));
        out.put("userId", u.getId());
        out.put("name", u.getName());
        out.put("email", u.getEmail());
        return out;
    }
}
