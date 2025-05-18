package com.cyberlog.Controllers.Blog;

import com.cyberlog.Models.Article;
import com.cyberlog.Models.Collection;
import com.cyberlog.Models.User;
import com.cyberlog.Repositories.Article.ArticleRepositoryCRUD;
import com.cyberlog.Repositories.CollectionRepository;
import com.cyberlog.Repositories.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/blog")
public class BlogController {

    @Autowired
    private CollectionRepository blogRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ArticleRepositoryCRUD articleRepo;

    @GetMapping("/create")
    public String viewCreate(Model model) {
        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        // Add a new blog object to the model
        model.addAttribute("blog", new Collection());

        return "blog/create";
    }

    @PostMapping("/create")
    public String createBlog(@ModelAttribute Collection blog) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userRepo.findUserByEmail(userEmail);

        blog.setUser(user);

        if (blog.getSlug() == null || blog.getSlug().isEmpty()) {
            String baseSlug = blog.getTitle().toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .replaceAll("\\s+", "-");

            String tempSlug = baseSlug;
            int counter = 1;
            while (blogRepository.existsBySlug(tempSlug)) {
                tempSlug = baseSlug + "-" + counter++;
            }

            blog.setSlug(tempSlug);
        }

        blogRepository.save(blog);
        return "redirect:/blog/list";
    }


    @GetMapping("/list")
    public String listBlogs(Model model) {
        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userRepo.findUserByEmail(userEmail);

        // Get all blogs for the user
        List<Collection> blogs = blogRepository.findByUser(user);
        model.addAttribute("blogs", blogs);

        return "blog/list";
    }

    @GetMapping("/{id}")
    public String viewBlog(@PathVariable("id") UUID id, Model model) {
        Collection blog = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found"));

        List<Article> articles = articleRepo.findByCollection(blog); // 🔍 aquí es donde carga artículos del blog

        model.addAttribute("blog", blog);
        model.addAttribute("articles", articles); // 👈 ¡Esto es lo que necesitas para mostrar artículos!

        return "blog/view";
    }



    @GetMapping("/update/{id}")
    public String viewUpdate(@PathVariable UUID id, Model model) {
        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userRepo.findUserByEmail(userEmail);

        // Find the blog
        Collection blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        // Check if blog belongs to the user
        if (!blog.getUser().getId().equals(user.getId())) {
            return "redirect:/error?message=You don't have permission to update this blog";
        }

        model.addAttribute("blog", blog);
        return "blog/update";
    }

    @PostMapping("/update/{id}")
    public String updateBlog(@PathVariable UUID id, @ModelAttribute Collection updatedBlog) {
        // Get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userRepo.findUserByEmail(userEmail);

        // Find the blog
        Collection blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        // Check if blog belongs to the user
        if (!blog.getUser().getId().equals(user.getId())) {
            return "redirect:/error?message=You don't have permission to update this blog";
        }

        // Update blog fields
        blog.setTitle(updatedBlog.getTitle());
        blog.setSummary(updatedBlog.getSummary());
        blog.setStatus(updatedBlog.getStatus());
        blog.setUpdated_at(LocalDateTime.now());

        // Update slug if changed
        if (!updatedBlog.getSlug().equals(blog.getSlug())) {
            String tempSlug = updatedBlog.getSlug();
            int counter = 1;
            while (blogRepository.existsBySlug(tempSlug) && !tempSlug.equals(blog.getSlug())) {
                tempSlug = updatedBlog.getSlug() + "-" + counter++;
            }
            blog.setSlug(tempSlug);
        }

        // Save the updated blog
        blogRepository.save(blog);

        return "redirect:/blog/list";
    }

    @PostMapping("/delete/{id}")
    @Transactional
    public String deleteBlog(@PathVariable UUID id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userRepo.findUserByEmail(userEmail);

        Collection blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        if (!blog.getUser().getId().equals(user.getId())) {
            return "redirect:/error?message=You don't have permission to delete this blog";
        }

        articleRepo.deleteAllByCollection(blog);

        blogRepository.delete(blog);

        return "redirect:/blog/list";
    }

    @ResponseBody
    @GetMapping("/api/list")
    public ResponseEntity<Map<String, Object>> getBlogs() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userRepo.findUserByEmail(userEmail);

        Map<String, Object> response = new HashMap<>();
        response.put("blogs", blogRepository.findByUser(user));

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}