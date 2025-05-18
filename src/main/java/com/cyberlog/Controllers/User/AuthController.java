package com.cyberlog.Controllers.User;

import com.cyberlog.Models.User;
import com.cyberlog.Service.JWTService;
import com.cyberlog.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private UserService service;

    @Autowired
    private JWTService jwtService;

    // Endpoint para mostrar la página de login
    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";  // Esto buscará login.html en src/main/resources/templates/
    }

    @PostMapping("/login-page")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody User user, HttpServletResponse response) {
        String jwt = service.verify(user);
        if ("fail".equals(jwt)) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid credentials");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // Crear la cookie HTTP-only para el JWT
        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);  // Previene el acceso desde JavaScript
        cookie.setSecure(false);   // Usar HTTPS (cámbialo a true en producción)
        cookie.setPath("/");       // Asegura que la cookie esté disponible para todas las rutas
        cookie.setMaxAge(60 * 60 * 24); // Duración de la cookie en segundos (1 día)
        response.addCookie(cookie);

        // Responder con un mensaje exitoso sin el JWT en el cuerpo
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", "Login successful");
        return ResponseEntity.ok(responseMap);
    }
    public boolean isValidUsername(String username) {
        return username != null && username.matches("[a-zA-Z0-9_-]+");
    }
    public String normalizeUsername(String input) {
        if (input == null) return null;
        return input.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9_-]", ""); // sólo letras, números, guiones y guion bajo
    }

    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String bio,
            @RequestParam String password,
            @RequestParam(required = false) String socialLinks) {

        User user = new User();
        String normalizedName = normalizeUsername(name);
        if (!isValidUsername(normalizedName)) {
            throw new RuntimeException("Nombre de usuario inválido");
        }
        user.setName(normalizedName);
        user.setEmail(email);
        user.setBio(bio);
        user.setPassword(password);

        if (socialLinks != null && !socialLinks.isBlank()) {
            user.setSocialLinks(socialLinks.trim());
        }
        User registeredUser = service.register(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Usuario registrado correctamente");
        response.put("user", registeredUser);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-auth")
    @ResponseBody
    public ResponseEntity<?> testAuth(HttpServletRequest request) {
        // Obtener el token desde la cookie
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "No authentication token found");
            return ResponseEntity.status(401).body(errorResponse);
        }

        // Validar el token con el JWTService
        if (jwtService.validateToken(token)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "You are authenticated!");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid token.");
            return ResponseEntity.status(401).body(errorResponse);  // 401 Unauthorized
        }
    }

    @PostMapping("/api/logout")
    @ResponseBody
    public ResponseEntity<?> logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", "Logged out successfully");
        return ResponseEntity.ok(responseMap);
    }
}