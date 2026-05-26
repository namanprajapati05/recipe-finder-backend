package com.example.recipefinder.controller;

import com.example.recipefinder.entity.User;
import com.example.recipefinder.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth") // '/auth' se badalkar '/api/auth' kiya taaki RecipeController se match kare
@CrossOrigin(origins = "*") // Standard format me CORS allow kiya
public class AuthController {

    private final AuthService authService;

    // Ekdum normal simple constructor injection
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 1. Signup Endpoint
    // URL: http://localhost:8080/api/auth/signup
    @PostMapping("/signup")
    public String signup(@RequestBody User user){
        return authService.signup(user);
    }

    // 2. Login Endpoint
    // URL: http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public String login(@RequestBody User user){
        return authService.login(user);
    }
}