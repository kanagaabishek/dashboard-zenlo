package com.example.demo.controller;

import com.example.demo.models.User;
import com.example.demo.service.AuthService;
import com.example.demo.security.JwtUtil;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    
    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }
    
    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Map<String, String> body) {
        User user = authService.register(body.get("email"), body.get("password"));
        String token = jwtUtil.generateToken(user.getEmail());
        return Map.of("token", token, "email", user.getEmail());
    }
    
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> body) {
        User user = authService.authenticate(body.get("email"), body.get("password"));
        String token = jwtUtil.generateToken(user.getEmail());
        return Map.of("token", token, "email", user.getEmail());
    }
}
