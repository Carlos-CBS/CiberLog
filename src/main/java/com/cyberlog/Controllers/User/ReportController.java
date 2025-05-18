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

        return "redirect:" + request.getHeader("Referer");
    }

    @GetMapping("/admin/reports")
    public String viewReports(Model model) {
        model.addAttribute("reports", reportRepository.findAllByOrderByCreatedAtDesc());
        return "admin/reports";
    }

    @PostMapping("/admin/reports/delete-comment")
    public String deleteCommentFromReport(@RequestParam UUID commentId, @RequestParam UUID reportId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();

        // Guardar historial antes de eliminar
        DeletedRecord history = DeletedRecord.builder()
                .recordType("comment")
                .originalId(comment.getId())
                .content(comment.getContent())
                .authorName(comment.getUser().getName())
                .createdAt(comment.getCreatedAt())
                .deletedAt(LocalDateTime.now())
                .build();

        deletedRecordRepository.save(history);

        // Eliminar reports asociados al comentario para evitar constraint
        List<Report> reports = reportRepository.findByComment(comment);
        reportRepository.deleteAll(reports);

        // Eliminar likes y útiles
        commentLikeRepository.deleteAll(commentLikeRepository.findByComment(comment));
        commentUsefulRepository.deleteAll(commentUsefulRepository.findByComment(comment));

        // Finalmente eliminar el comentario
        commentRepository.delete(comment);

        // Marcar el reporte original como resuelto
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
                .build();

        deletedRecordRepository.save(history);

        // Eliminar reports asociados al artículo para evitar constraint
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
        reportRepository.findById(reportId).ifPresent(report -> {
            report.setStatus(Report.Status.dismissed);
            reportRepository.save(report);
        });
        return "redirect:/admin/reports";
    }

    @GetMapping("/admin/deleted-records")
    public String viewDeletedRecords(Model model) {
        model.addAttribute("deletedRecords", deletedRecordRepository.findAll());
        return "admin/deleted-records";
    }


}
