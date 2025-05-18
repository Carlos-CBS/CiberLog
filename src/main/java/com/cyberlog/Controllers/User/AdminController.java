package com.cyberlog.Controllers.User;

import com.cyberlog.Models.User;
import com.cyberlog.Repositories.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepo userRepo;

    // Listado de usuarios
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepo.findAll());
        return "admin/users";
    }

    // Mostrar formulario de edición
    @GetMapping("/users/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model) {
        User user = userRepo.findById(id).orElseThrow();
        model.addAttribute("user", user);
        return "admin/edit-user";
    }

    // Procesar edición
    @PostMapping("/users/edit")
    public String updateUser(@ModelAttribute User user) {
        // Cargar el usuario real de la BD por ID
        User existingUser = userRepo.findById(user.getId()).orElseThrow();

        // Actualizar campos permitidos
        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setBio(user.getBio());
        existingUser.setSocialLinks(user.getSocialLinks());
        existingUser.setRole(user.getRole()); // Asegúrate de que el rol es seguro

        userRepo.save(existingUser);

        return "redirect:/admin/users";
    }


    // Actualizar rol
    @PostMapping("/users/update/{id}")
    public String updateUserRole(@PathVariable UUID id, @RequestParam User.Role role) {
        User user = userRepo.findById(id).orElseThrow();
        user.setRole(role); // Asegúrate de usar "ROLE_ADMIN" o "ROLE_USER"
        userRepo.save(user);
        return "redirect:/admin/users";
    }

    // Eliminar usuario
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable UUID id, HttpServletRequest request) {
        userRepo.deleteById(id);
        return "redirect:/admin/users";
    }
}
