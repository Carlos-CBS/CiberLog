package com.cyberlog.Controllers.User;

import com.cyberlog.Models.User;
import com.cyberlog.Repositories.UserRepo;
import com.cyberlog.Service.JWTService;
import com.cyberlog.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Autowired
    private UserService service;

    @Autowired
    private JWTService jwtService;
    @Autowired
    private UserRepo userRepo;

    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
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

        Cookie cookie = new Cookie("jwt", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60 * 24);
        response.addCookie(cookie);

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
                .replaceAll("[^a-z0-9_-]", "");
    }

    private boolean isStrongPassword(String password) {
        if (password == null || password.length() < 12) return false;

        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-={}:\";'<>?,./\\\\|\\[\\]].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }


    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String bio,
            @RequestParam String password,
            @RequestParam(required = false) String socialLinks, Model model,
            RedirectAttributes redirectAttributes) {

        User user = new User();
        String normalizedName = normalizeUsername(name);
        User userReg = userRepo.findUserByName(name);
        User userReg2 = userRepo.findUserByEmail(email);
        if (userReg != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nombre de usuario ya existe, por favor intentelo de nuevo.");
            return "redirect:/register";
        }
        if (userReg2 != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email ya existe, por favor intentelo de nuevo.");
            return "redirect:/register";
        }

        if (!isStrongPassword(password)) {
            redirectAttributes.addFlashAttribute("errorMessage", "La contraseña no cumple con los requisitos de seguridad.");
            return "redirect:/register";
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

        return "redirect:/login";
    }

    @GetMapping("/test-auth")
    @ResponseBody
    public ResponseEntity<?> testAuth(HttpServletRequest request) {

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

        if (jwtService.validateToken(token)) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "You are authenticated!");
            return ResponseEntity.ok(response);
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid token.");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    @PostMapping("/api/logout")
    public String logout(HttpServletResponse response) {

        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("message", "Logged out exitosamente");
        return "redirect:/article/home";
    }

    @GetMapping("/")
    public String showHomePage() {
        return "redirect:article/home";
    }
}