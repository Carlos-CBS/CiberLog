package com.cyberlog.Controllers.User;

import com.cyberlog.Models.*;
import com.cyberlog.Repositories.*;
import com.cyberlog.Repositories.Article.ArticleRepositoryCRUD;
import com.cyberlog.Repositories.Article.ArticleViewRepository;
import com.cyberlog.Repositories.Comment.CommentLikeRepository;
import com.cyberlog.Repositories.Comment.CommentRepository;
import com.cyberlog.Repositories.Comment.CommentUsefulRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.cyberlog.Controllers.User.UserController.md5Hex;

@Controller
@RequiredArgsConstructor
public class ReportController {

    @Autowired
    private reportRepository reportRepository;


    @Autowired
    private ArticleRepositoryCRUD articleRepository;


    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleViewRepository articleViewRepository;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentUsefulRepository commentUsefulRepository;

    @Autowired
    private DeletedRecordRepository deletedRecordRepository;

    @PostMapping("/report/article")
    public String reportArticle(@RequestParam UUID articleId,
                                @RequestParam String content,
                                HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findUserByEmail(auth.getName());

        Article article = articleRepository.findById(articleId).orElseThrow();

        Report report = Report.builder()
                .user(user)
                .article(article)
                .reportType(Report.ReportType.article)
                .content(content)
                .status(Report.Status.pending)
                .createdAt(LocalDateTime.now())
                .build();

        reportRepository.save(report);

        return "redirect:" + request.getHeader("Referer");
    }

    @PostMapping("/report/comment")
    public String reportComment(@RequestParam UUID commentId,
                                @RequestParam String content,
                                HttpServletRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findUserByEmail(auth.getName());

        Comment comment = commentRepository.findById(commentId).orElseThrow();

        Report report = Report.builder()
                .user(user)
                .comment(comment)
                .reportType(Report.ReportType.comment)
                .content(content)
                .status(Report.Status.pending)
                .createdAt(LocalDateTime.now())
                .build();

        reportRepository.save(report);

        return "redirect:/article/view/" + comment.getArticle().getUser().getName() + "/" + comment.getArticle().getSlug();
    }

    @GetMapping("/admin/reports")
    public String viewReports(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findUserByEmail(auth.getName());

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("user", user);
        model.addAttribute("reports", reportRepository.findAllByOrderByCreatedAtDesc());
        return "admin/reports";
    }

    @PostMapping("/admin/reports/delete-comment")
    public String deleteCommentFromReport(@RequestParam UUID commentId, @RequestParam UUID reportId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();


        DeletedRecord history = DeletedRecord.builder()
                .recordType("comment")
                .originalId(comment.getId())
                .content(comment.getContent())
                .authorName(comment.getUser().getName())
                .createdAt(comment.getCreatedAt())
                .deletedAt(LocalDateTime.now())
                .status(Report.Status.resolved)
                .build();

        deletedRecordRepository.save(history);

        List<Report> reports = reportRepository.findByComment(comment);
        reportRepository.deleteAll(reports);
        commentLikeRepository.deleteAll(commentLikeRepository.findByComment(comment));
        commentUsefulRepository.deleteAll(commentUsefulRepository.findByComment(comment));

        commentRepository.delete(comment);

        reportRepository.findById(reportId).ifPresent(report -> {
            report.setStatus(Report.Status.resolved);
            reportRepository.save(report);
        });

        return "redirect:/admin/reports";
    }


    @PostMapping("/admin/reports/delete-article")
    @Transactional
    public String deleteArticleFromReport(@RequestParam UUID articleId, @RequestParam UUID reportId, Model model) {
        Article article = articleRepository.findById(articleId).orElseThrow();

        DeletedRecord history = DeletedRecord.builder()
                .recordType("article")
                .originalId(article.getId())
                .content(article.getContent())
                .authorName(article.getUser().getName())
                .createdAt(article.getCreated_at())
                .deletedAt(LocalDateTime.now())
                .status(Report.Status.resolved)
                .build();

        deletedRecordRepository.save(history);

        List<Report> reports = reportRepository.findByArticle(article);
        reportRepository.deleteAll(reports);
        articleViewRepository.deleteAllByArticle(article);
        articleRepository.delete(article);

        reportRepository.findById(reportId).ifPresent(report -> {
            report.setStatus(Report.Status.resolved);
            reportRepository.save(report);
        });

        model.addAttribute("reports", reportRepository.findAllByOrderByCreatedAtDesc());
        return "redirect:/admin/reports";
    }




    @PostMapping("/admin/reports/dismiss")
    public String dismissReport(@RequestParam UUID reportId) {

        Report report = reportRepository.findById(reportId).orElseThrow();

        DeletedRecord history = DeletedRecord.builder()
                .recordType(String.valueOf(report.getReportType()))
                .originalId(report.getId())
                .content(report.getContent())
                .authorName(report.getUser() != null ? report.getUser().getName() : "unknown")
                .createdAt(report.getCreatedAt())
                .deletedAt(LocalDateTime.now())
                .status(Report.Status.dismissed)
                .build();

        deletedRecordRepository.save(history);

        reportRepository.delete(report);

        return "redirect:/admin/reports";
    }


    @GetMapping("/admin/deleted-records")
    public String viewDeletedRecords(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findUserByEmail(auth.getName());

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("user", user);
        model.addAttribute("deletedRecords", deletedRecordRepository.findAll());
        return "admin/deleted-records";
    }


}
