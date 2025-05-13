package com.cyberlog.Controllers;

import com.cyberlog.Models.User;
import com.cyberlog.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService service;

    // Endpoint para mostrar la página de login
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";  // Esto buscará login.html en src/main/resources/templates/
    }

    // Endpoint API para el login (usado por JavaScript)
    @PostMapping("/login-page")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody User user) {
        String jwt = service.verify(user);
        if ("fail".equals(jwt)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid credentials");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        return ResponseEntity.ok(response);
    }

    // Endpoint de registro
    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> register(@RequestBody User user) {
        User registeredUser = service.register(user);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("user", registeredUser);
        return ResponseEntity.ok(response);
    }

    // Endpoint de prueba para verificar la autenticación
    @GetMapping("/test-auth")
    @ResponseBody
    public ResponseEntity<?> testAuth() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "You are authenticated!");
        return ResponseEntity.ok(response);
    }
}
