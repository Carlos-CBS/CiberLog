package com.cyberlog.Controllers;

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
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private JWTService jwtService;

    // Endpoint para mostrar la página de login
    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";  // Esto buscará login.html en src/main/resources/templates/
    }

    // Endpoint API para el login (usado por JavaScript)
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

    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password) {

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);

        User registeredUser = service.register(user);

        // Preparar la respuesta
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Usuario registrado correctamente");
        response.put("user", registeredUser);

        return ResponseEntity.ok(response);
    }



//    @PostMapping("/register")
//    @ResponseBody
//    public ResponseEntity<?> register(@RequestBody User user) {
//        User registeredUser = service.register(user);
//        Map<String, Object> response = new HashMap<>();
//        response.put("message", "User registered successfully");
//        response.put("user", registeredUser);
//        return ResponseEntity.ok(response);
//    }

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

        // Si no se encuentra el token
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
        System.out.println("Logout request received");  // Log para verificar que la ruta está siendo llamada

        // Crear una cookie con el mismo nombre pero sin valor y expiración inmediata
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Si usas HTTPS, ponlo como true
        cookie.setPath("/"); // Asegura que la cookie sea válida para todo el sitio
        cookie.setMaxAge(0); // Eliminar la cookie inmediatamente
        response.addCookie(cookie);

        // Responder con un mensaje de éxito
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", "Logged out successfully");
        return ResponseEntity.ok(responseMap);
    }
}