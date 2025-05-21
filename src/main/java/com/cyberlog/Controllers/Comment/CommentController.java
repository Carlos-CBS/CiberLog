package com.cyberlog.Controllers.Comment;

import com.cyberlog.Models.*;
import com.cyberlog.Repositories.Article.ArticleLikeRepository;
import com.cyberlog.Repositories.Article.ArticleRepositoryCRUD;
import com.cyberlog.Repositories.Comment.CommentLikeRepository;
import com.cyberlog.Repositories.Comment.CommentRepository;
import com.cyberlog.Repositories.Comment.CommentUsefulRepository;
import com.cyberlog.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import static com.cyberlog.Controllers.User.UserController.md5Hex;

@Controller
public class CommentController {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentUsefulRepository commentUsefulRepository;


    @Autowired
    private ArticleRepositoryCRUD articleRepo;

    @PostMapping("/comment/add")
    public String addComment(@RequestParam UUID articleId,
                             @RequestParam String content,
                             @RequestParam(required = false) UUID parentCommentId,
                             Principal principal) {

        User user = userRepo.findUserByEmail(principal.getName());
        Optional<Article> optionalArticle = articleRepo.findById(articleId);

        if (optionalArticle.isEmpty()) {
            return "redirect:/blog/list";
        }

        Article article = optionalArticle.get();

        Comment.CommentBuilder commentBuilder = Comment.builder()
                .article(article)
                .user(user)
                .content(content)
                .commentLike(0)
                .commentUseful(0);

        if (parentCommentId != null) {
            Optional<Comment> parent = commentRepository.findById(parentCommentId);
            parent.ifPresent(commentBuilder::parentComment);
        }

        commentRepository.save(commentBuilder.build());

        return "redirect:/article/view/" + article.getUser().getName() + "/" + article.getSlug();
    }

    @PostMapping("/comment/delete")
    public String deleteComment(@RequestParam UUID commentId, Principal principal) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) return "redirect:/blog/list";

        Comment comment = commentOpt.get();

        if (!comment.getUser().getEmail().equals(principal.getName())) {
            return "redirect:/unauthorized"; // o manejarlo mejor
        }

        commentLikeRepository.deleteAll(commentLikeRepository.findByComment(comment));

        commentUsefulRepository.deleteAll(commentUsefulRepository.findByComment(comment));

        commentRepository.delete(comment);

        return "redirect:/article/view/" + comment.getArticle().getUser().getName() + "/" + comment.getArticle().getSlug();
    }

    @PostMapping("/comment/edit")
    public String editComment(@RequestParam UUID commentId,
                              @RequestParam String content,
                              Principal principal) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) return "redirect:/blog/list";

        Comment comment = commentOpt.get();

        if (!comment.getUser().getEmail().equals(principal.getName())) {
            return "redirect:/unauthorized";
        }

        comment.setContent(content);
        commentRepository.save(comment);

        return "redirect:/article/view/" + comment.getArticle().getUser().getName() + "/" + comment.getArticle().getSlug();
    }

    @PostMapping("/comment/like")
    public String toggleLikeComment(@RequestParam UUID commentId, Principal principal) {
        User user = userRepo.findUserByEmail(principal.getName());
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        Optional<CommentLike> existingLike = commentLikeRepository.findByUserAndComment(user, comment);

        if (existingLike.isPresent()) {

            commentLikeRepository.delete(existingLike.get());
            comment.setCommentLike(comment.getCommentLike() - 1);
        } else {

            CommentLike cl = CommentLike.builder().user(user).comment(comment).build();
            commentLikeRepository.save(cl);
            comment.setCommentLike(comment.getCommentLike() + 1);
        }

        commentRepository.save(comment);

        return "redirect:/article/view/" + comment.getArticle().getUser().getName() + "/" + comment.getArticle().getSlug();
    }

    @PostMapping("/comment/useful")
    public String toggleUsefulComment(@RequestParam UUID commentId, Principal principal) {
        User user = userRepo.findUserByEmail(principal.getName());
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentario no encontrado"));

        Optional<CommentUseful> existingUseful = commentUsefulRepository.findByUserAndComment(user, comment);

        if (existingUseful.isPresent()) {
            commentUsefulRepository.delete(existingUseful.get());
            comment.setCommentUseful(comment.getCommentUseful() - 1);
        } else {

            CommentUseful cu = CommentUseful.builder().user(user).comment(comment).build();
            commentUsefulRepository.save(cu);
            comment.setCommentUseful(comment.getCommentUseful() + 1);
        }

        commentRepository.save(comment);

        return "redirect:/article/view/" + comment.getArticle().getUser().getName() + "/" + comment.getArticle().getSlug();
    }
    @GetMapping("/general/faqs")
    public String faqs(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("user", user);
        return "/faqs";
    }

    @GetMapping("/general/consejos")
    public String consejos(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());
        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("user", user);
        return "/general/consejos";
    }
}


