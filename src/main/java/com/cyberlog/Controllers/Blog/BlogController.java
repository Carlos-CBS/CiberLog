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

import static com.cyberlog.Controllers.User.UserController.md5Hex;

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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userRepo.findUserByEmail(userEmail);

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("user", user);
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

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userRepo.findUserByEmail(userEmail);

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        List<Collection> blogs = blogRepository.findByUser(user);
        model.addAttribute("blogs", blogs);
        model.addAttribute("user", user);
        return "blog/list";
    }

    @GetMapping("/{id}")
    public String viewBlog(@PathVariable("id") UUID id, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userRepo.findUserByEmail(userEmail);


        Collection blog = blogRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found"));

        List<Article> articles = articleRepo.findByCollection(blog);

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("blog", blog);
        model.addAttribute("articles", articles);
        model.addAttribute("user", user);
        return "blog/view";
    }



    @GetMapping("/update/{id}")
    public String viewUpdate(@PathVariable UUID id, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userRepo.findUserByEmail(userEmail);

        Collection blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        if (!blog.getUser().getId().equals(user.getId())) {
            return "redirect:/error?message=You don't have permission to update this blog";
        }

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("blog", blog);
        model.addAttribute("user", user);
        return "blog/update";
    }

    @PostMapping("/update/{id}")
    public String updateBlog(@PathVariable UUID id, @ModelAttribute Collection updatedBlog) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User user = userRepo.findUserByEmail(userEmail);

        Collection blog = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));

        if (!blog.getUser().getId().equals(user.getId())) {
            return "redirect:/error?message=You don't have permission to update this blog";
        }

        blog.setTitle(updatedBlog.getTitle());
        blog.setSummary(updatedBlog.getSummary());
        blog.setStatus(updatedBlog.getStatus());
        blog.setUpdated_at(LocalDateTime.now());

        if (!updatedBlog.getSlug().equals(blog.getSlug())) {
            String tempSlug = updatedBlog.getSlug();
            int counter = 1;
            while (blogRepository.existsBySlug(tempSlug) && !tempSlug.equals(blog.getSlug())) {
                tempSlug = updatedBlog.getSlug() + "-" + counter++;
            }
            blog.setSlug(tempSlug);
        }

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
}