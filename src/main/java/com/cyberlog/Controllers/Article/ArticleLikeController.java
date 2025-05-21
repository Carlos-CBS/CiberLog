package com.cyberlog.Controllers.Article;

import com.cyberlog.Models.Article;
import com.cyberlog.Models.ArticleLike;
import com.cyberlog.Models.User;
import com.cyberlog.Repositories.Article.ArticleLikeRepository;
import com.cyberlog.Repositories.Article.ArticleRepositoryCRUD;
import com.cyberlog.Repositories.UserRepo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/article")
public class ArticleLikeController {

    @Autowired
    private ArticleRepositoryCRUD articleRepo;

    @Autowired
    private ArticleLikeRepository likeRepo;

    @Autowired
    private UserRepo userRepo;

    @PostMapping("/like/{id}")
    public String toggleLike(@PathVariable("id") String articleId,
                           HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());

        Article article = articleRepo.findById(UUID.fromString(articleId))
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        Optional<ArticleLike> existingLike = likeRepo.findByUserAndArticle(user, article);

        if (existingLike.isPresent()) {
            likeRepo.delete(existingLike.get());
        } else {
            ArticleLike newLike = ArticleLike.builder()
                    .article(article)
                    .user(user)
                    .build();
            likeRepo.save(newLike);
        }

        long newLikesCount = likeRepo.countByArticle(article);
        article.setLikesCount((int) newLikesCount);
        articleRepo.save(article);

        return "redirect:/article/view/" + article.getUser().getName() + "/" + article.getSlug();
    }
}
