package com.cyberlog.Controllers.User;

import com.cyberlog.Models.User;
import com.cyberlog.Repositories.Article.ArticleLikeRepository;
import com.cyberlog.Repositories.Article.ArticleRepositoryCRUD;
import com.cyberlog.Repositories.Article.ArticleUsefulRepository;
import com.cyberlog.Repositories.Article.ArticleViewRepository;
import com.cyberlog.Repositories.CollectionRepository;
import com.cyberlog.Repositories.Comment.CommentLikeRepository;
import com.cyberlog.Repositories.Comment.CommentRepository;
import com.cyberlog.Repositories.Comment.CommentUsefulRepository;
import com.cyberlog.Repositories.UserBookmarkRepo;
import com.cyberlog.Repositories.UserRepo;
import com.cyberlog.Repositories.reportRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.util.UUID;

import static com.cyberlog.Controllers.User.UserController.md5Hex;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ArticleRepositoryCRUD articleRepositoryCRUD;
    @Autowired
    private CollectionRepository collectionRepository;
    @Autowired
    private ArticleViewRepository articleViewRepository;
    @Autowired
    private ArticleUsefulRepository articleUsefulRepository;
    @Autowired
    private ArticleLikeRepository articleLikeRepository;
    @Autowired
    private reportRepository reportRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserBookmarkRepo userBookmarkRepo;
    @Autowired
    private CommentLikeRepository commentLikeRepository;
    @Autowired
    private CommentUsefulRepository commentUsefulRepository;


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

    @GetMapping("/users")
    public String listUsers(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("user", user);
        model.addAttribute("users", userRepo.findAll());
        model.addAttribute("articles", userRepo.findAll());
        return "admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String showEditForm(@PathVariable UUID id, Model model) {
        User user = userRepo.findById(id).orElseThrow();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("user", user);
        return "admin/edit-user";
    }

    @PostMapping("/users/edit")
    public String updateUser(@ModelAttribute User user) {

        User existingUser = userRepo.findById(user.getId()).orElseThrow();

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());
        existingUser.setBio(user.getBio());
        existingUser.setSocialLinks(user.getSocialLinks());
        existingUser.setRole(user.getRole());

        userRepo.save(existingUser);

        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    @Transactional
    public String deleteUser(@PathVariable UUID id, HttpServletRequest request) {

        User user = userRepo.findById(id).orElseThrow();

        articleViewRepository.deleteByUser(user);
        userBookmarkRepo.deleteByUser(user);
        commentLikeRepository.deleteByUser(user);
        commentUsefulRepository.deleteByUser(user);
        commentRepository.deleteByUser(user);
        reportRepository.deleteByUser(user);
        articleLikeRepository.deleteByUser(user);
        articleUsefulRepository.deleteByUser(user);
        articleRepositoryCRUD.deleteByUser(user);
        collectionRepository.deleteByUser(user);
        userRepo.deleteById(id);
        return "redirect:/admin/users";
    }

}
