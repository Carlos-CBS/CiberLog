package com.cyberlog.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @ManyToOne
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @ManyToOne
    @JoinColumn(name = "article_id")
    private Article article;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        pending, reviewed, resolved, dismissed
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.pending;

    public enum ReportType {
        article, comment, user
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType;

    // Método que asegura que sólo uno de los campos esté completo según el tipo de reporte
    @PrePersist
    @PreUpdate
    private void validateReportType() {
        switch (this.reportType) {
            case article:
                if (this.article == null) throw new IllegalArgumentException("Article must be specified for report type 'article'");
                break;
            case comment:
                if (this.comment == null) throw new IllegalArgumentException("Comment must be specified for report type 'comment'");
                break;
            case user:
                if (this.reportedUser == null) throw new IllegalArgumentException("User must be specified for report type 'user'");
                break;
        }
    }
}
