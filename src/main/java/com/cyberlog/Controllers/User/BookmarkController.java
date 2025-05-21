package com.cyberlog.Controllers.User;

import com.cyberlog.Models.Article;
import com.cyberlog.Models.User;
import com.cyberlog.Models.UserBookmark;
import com.cyberlog.Repositories.Article.ArticleRepositoryCRUD;
import com.cyberlog.Repositories.UserBookmarkRepo;
import com.cyberlog.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.cyberlog.Controllers.User.UserController.md5Hex;

@Controller
@RequestMapping("/bookmarks")
public class BookmarkController {

    @Autowired
    private UserBookmarkRepo bookmarkRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ArticleRepositoryCRUD articleRepo;

    @GetMapping("/list")
    public String list(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());

        List<UserBookmark> userBookmarks = bookmarkRepo.findByUser(user);

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("bookmarks", userBookmarks);
        model.addAttribute("user", user);
        return "bookmarks/list";
    }

    @PostMapping("add/{articleId}")
    public String addBookmark(@PathVariable UUID articleId, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());

        Article article = articleRepo.findById(articleId).orElseThrow();

        UserBookmark userBookmark = new UserBookmark();
        userBookmark.setUser(user);
        userBookmark.setArticle(article);
        bookmarkRepo.save(userBookmark);

        model.addAttribute("bookmark", bookmarkRepo.findByUser(user));
        return "redirect:/bookmarks/list";
    }

    @PostMapping("/delete/{articleId}")
    public String deleteBookmark(@PathVariable UUID articleId) {

        Article article = articleRepo.findById(articleId).orElseThrow();
        UserBookmark bookmark = bookmarkRepo.findByArticle(article);
        bookmarkRepo.delete(bookmark);
        return "redirect:/bookmarks/list";
    }
}

