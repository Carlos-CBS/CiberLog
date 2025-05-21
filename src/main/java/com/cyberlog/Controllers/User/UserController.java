package com.cyberlog.Controllers.User;

import com.cyberlog.Models.User;
import com.cyberlog.Repositories.UserRepo;
import com.cyberlog.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.MessageDigest;
import java.security.Principal;
import java.util.Optional;


@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepo userRepo;

    public static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.trim().toLowerCase().getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    @GetMapping("/bio")
    public String bio(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("user", user);
        model.addAttribute("gravatar", gravatarUrl);
        return "general/bio";
    }


    @PostMapping("/edit")
    public String editProfile(
            @RequestParam(required = false) String name,
            @RequestParam String bio,
            @RequestParam String socialLinks,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User user = userRepo.findUserByEmail(principal.getName());

        if (name != null && !user.getName().equals(name)) {
            Optional<User> existingUser = userRepo.findByName(name);

            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "El nombre de usuario ya está en uso.");
                return "redirect:/user/bio";
            }

            user.setName(name);
        }

        user.setBio(bio);
        user.setSocialLinks(socialLinks);
        userRepo.save(user);

        redirectAttributes.addFlashAttribute("success", "Perfil actualizado correctamente.");
        return "redirect:/user/bio";
    }

    @GetMapping("/{name}/bio")
    public String bio(
            @PathVariable String name,
            Model model) {
        User user = userRepo.findUserByName(name);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("user", user);
        model.addAttribute("gravatar", gravatarUrl);
        return "general/bio";
    }
}
