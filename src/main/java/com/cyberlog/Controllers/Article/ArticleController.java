package com.cyberlog.Controllers.Article;

import com.cyberlog.Models.*;
import com.cyberlog.Models.Collection;
import com.cyberlog.Repositories.Article.ArticleLikeRepository;
import com.cyberlog.Repositories.Article.ArticleRepositoryCRUD;
import com.cyberlog.Repositories.Article.ArticleUsefulRepository;
import com.cyberlog.Repositories.Article.ArticleViewRepository;
import com.cyberlog.Repositories.CollectionRepository;
import com.cyberlog.Repositories.Comment.CommentLikeRepository;
import com.cyberlog.Repositories.Comment.CommentRepository;
import com.cyberlog.Repositories.Comment.CommentUsefulRepository;
import com.cyberlog.Repositories.TagRepository;
import com.cyberlog.Repositories.UserRepo;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import static com.cyberlog.Controllers.User.UserController.md5Hex;

@Controller
@RequestMapping("/article")
public class ArticleController {


    @Autowired
    private CommentUsefulRepository commentUsefulRepository;
    @Autowired
    private CommentLikeRepository commentLikeRepository;

    public class MarkdownUtils {
        private static final Parser parser = Parser.builder().build();
        private static final HtmlRenderer renderer = HtmlRenderer.builder().build();

        public static String markdownToHtml(String markdown) {
            if (markdown == null) return "";
            Parser parser = Parser.builder().build();
            HtmlRenderer renderer = HtmlRenderer.builder().build();
            String html = renderer.render(parser.parse(markdown));

            Safelist safelist = Safelist.basic()
                    .addTags("h1", "h2", "h3", "h4", "h5", "h6", "pre", "code", "table", "thead", "tbody", "tr", "th", "td", "blockquote", "ul", "ol", "li")
                    .addAttributes("a", "href", "title")
                    .addAttributes("img", "src", "alt", "title")
                    .addProtocols("a", "href", "http", "https", "mailto")
                    .addProtocols("img", "src", "http", "https");

            return Jsoup.clean(html, safelist);
        }
    }

    @Autowired
    private ArticleRepositoryCRUD articleRepo;

    @Autowired
    private CollectionRepository collectionRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ArticleViewRepository ViewRepo;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleLikeRepository LikeRepo;
    @Autowired
    private ArticleLikeRepository articleLikeRepository;
    @Autowired
    private ArticleViewRepository articleViewRepository;
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ArticleUsefulRepository articleUsefulRepository;

    @GetMapping("/create")
    public String viewCreate(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("collections", collectionRepo.findByUser(user));
        model.addAttribute("article", new Article());
        model.addAttribute("allTags", tagRepository.findAll());
        model.addAttribute("user", user);

        return "article/create";
    }

    @GetMapping("/view/{name}/{articleSlug}")
    public String viewArticle(
            @PathVariable String name,
            @PathVariable String articleSlug,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) throws IOException {

        User user = userRepo.findByName(name)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Article article = articleRepo.findByUserAndSlug(user, articleSlug)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User viewer = null;
        String visitorId = null;

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            viewer = userRepo.findUserByEmail(auth.getName());
        } else {
            // Usuario anónimo: leer visitorId de cookie
            visitorId = getVisitorIdFromCookies(request);

            if (visitorId == null || !isValidUUID(visitorId)) {
                visitorId = UUID.randomUUID().toString();

                String cookieValue = "visitorId=" + visitorId +
                        "; Max-Age=" + (60 * 60 * 24 * 365) +
                        "; Path=/" +
                        "; Secure" +
                        "; HttpOnly" +
                        "; SameSite=Lax";

                response.addHeader("Set-Cookie", cookieValue);
            }

        }
        if (article.getStatus() == Article.Status.draft && (viewer == null || !viewer.equals(article.getUser()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Este artículo no está publicado.");
            return null;
        }

        boolean shouldCountView = false;

        if (viewer != null && !viewer.equals(article.getUser())) {

            Optional<ArticleView> lastView = ViewRepo.findTopByUserAndArticleOrderByViewedAtDesc(viewer, article);

            shouldCountView = lastView.isEmpty() ||
                    lastView.get().getViewedAt().isBefore(LocalDateTime.now().minusWeeks(1));

            if (shouldCountView) {
                ArticleView av = new ArticleView();
                av.setArticle(article);
                av.setUser(viewer);
                av.setViewedAt(LocalDateTime.now());
                articleViewRepository.save(av);
            }
        } else if (visitorId != null) {
            Optional<ArticleView> lastView = ViewRepo.findTopByVisitorIdAndArticleOrderByViewedAtDesc(visitorId, article);

            shouldCountView = lastView.isEmpty() ||
                    lastView.get().getViewedAt().isBefore(LocalDateTime.now().minusWeeks(1));

            if (shouldCountView) {
                ArticleView av = new ArticleView();
                av.setArticle(article);
                av.setVisitorId(visitorId);
                av.setViewedAt(LocalDateTime.now());
                articleViewRepository.save(av);
            }
        }

        long viewCount = articleViewRepository.countByArticle(article);
        article.setViews((int) viewCount);

        List<Comment> rootComments = commentRepository.findByArticle(article).stream()
                .filter(c -> c.getParentComment() == null)
                .toList();

        model.addAttribute("comments", rootComments);

        String htmlContent = MarkdownUtils.markdownToHtml(article.getContent());
        article.setContent(htmlContent);

        String gravatarHash = md5Hex(user.getEmail());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("article", article);
        model.addAttribute("collection", article.getCollection());
        model.addAttribute("user", user);
        model.addAttribute("currentUser", viewer);
        return "article/view";
    }

    private String getVisitorIdFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if ("visitorId".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean isValidUUID(String uuid) {
        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @PostMapping("/create")
    public String createArticle(
            @ModelAttribute("article") Article article,
            @RequestParam("collection.id") UUID collectionId,
            @RequestParam(name = "tags", required = false) List<UUID> tagIds
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());

        article.setUser(user);
        Collection col = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Colección no encontrada"));
        article.setCollection(col);

        if (tagIds != null && !tagIds.isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));
            article.setTags(tags);
        } else {
            article.setTags(new HashSet<>());
        }

        if (article.getSlug() == null || article.getSlug().isBlank()) {
            String base = normalizeSlug(article.getTitle());
            String temp = base;
            int i = 1;
            while (articleRepo.existsByCollectionAndSlug(col, temp)) {
                temp = base + "-" + i++;
            }
            article.setSlug(temp);
        } else {
            article.setSlug(normalizeSlug(article.getSlug()));
        }

        articleRepo.save(article);
        return "redirect:/article/list";
    }


    @GetMapping("/list")
    public String listArticles(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());
        List<Article> articles = articleRepo.findByUser(user);

        Map<UUID, List<Comment>> commentsMap = new HashMap<>();


        for (Article article : articles) {
            List<Comment> comments = commentRepository.findByArticleOrderByCreatedAtAsc(article);
            commentsMap.put(article.getId(), comments);
        }

        String gravatarHash = md5Hex(user.getEmail());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("articles", articles);
        model.addAttribute("commentsMap", commentsMap);
        model.addAttribute("user", user);
        return "article/list";
    }


    private String normalizeSlug(String input) {
        if (input == null) return null;
        return input.trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
    }

    @GetMapping("/update/{name}/{slug}")
    public String viewUpdate(@PathVariable String name,
                             @PathVariable String slug,
                             Model model,
                             HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User sessionUser = userRepo.findUserByEmail(auth.getName());

        User user = userRepo.findByName(name)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!sessionUser.equals(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        Article article = articleRepo.findByUserAndSlug(user, slug)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        String gravatarHash = md5Hex(user.getEmail());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        model.addAttribute("article", article);
        model.addAttribute("collections", collectionRepo.findByUser(user));
        model.addAttribute("user", user);

        return "article/update";
    }


    @PostMapping("/update/{slug}")
    public String updateArticle(
            @PathVariable String slug,
            @ModelAttribute("article") Article updated,
            @RequestParam("collection.id") UUID collectionId,
            HttpServletResponse response) throws IOException
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());


        Article art = articleRepo.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
        Collection col = collectionRepo.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Colección no encontrada"));

        if (!art.getUser().equals(user)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return null;
        }

        art.setTitle(updated.getTitle());
        art.setSummary(updated.getSummary());
        art.setContent(updated.getContent());
        art.setReading_time_minutes(updated.getReading_time_minutes());
        art.setStatus(updated.getStatus());
        art.setDifficulty(updated.getDifficulty());
        art.setCollection(col);

        if (updated.getSlug() != null && !updated.getSlug().equals(art.getSlug())) {
            art.setSlug(updated.getSlug());
        }

        articleRepo.save(art);
        return "redirect:/article/list";
    }

    @PostMapping("/delete/{name}/{slug}")
    @Transactional
    public String deleteArticle(@PathVariable String name,
                                @PathVariable String slug,
                                HttpServletResponse response) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User sessionUser = userRepo.findUserByEmail(auth.getName());

        User user = userRepo.findByName(name)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!sessionUser.equals(user)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        Article article = articleRepo.findByUserAndSlug(user, slug)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        articleViewRepository.deleteByArticleId(article.getId());
        articleLikeRepository.deleteAll(articleLikeRepository.findByArticle(article));
        articleUsefulRepository.deleteAll(articleUsefulRepository.findByArticle(article));

        List<Comment> comments = commentRepository.findByArticle(article);

        commentLikeRepository.deleteByCommentIn(comments);
        commentUsefulRepository.deleteByCommentIn(comments);

        commentRepository.deleteAll(commentRepository.findByArticle(article));

        articleRepo.delete(article);
        return "redirect:/article/list";
    }

    @GetMapping("/home")
    public String home(@RequestParam(name = "tagIds", required = false) List<UUID> tagIds,
                       @RequestParam(name = "sortBy", required = false, defaultValue = "newest") String sortBy,
                       Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());

        List<Article> articles;

        // Aplicar filtro por tags
        if (tagIds != null && !tagIds.isEmpty()) {
            articles = articleRepo.findByStatusAndTags_IdIn(Article.Status.published, tagIds);
        } else {
            articles = articleRepo.findByStatus(Article.Status.published);
        }

        // Aplicar ordenamiento según el filtro seleccionado
        switch (sortBy) {
            case "oldest":
                articles = articles.stream()
                        .sorted((a1, a2) -> a1.getCreated_at().compareTo(a2.getCreated_at()))
                        .collect(Collectors.toList());
                break;
            case "mostLiked":
                articles = articles.stream()
                        .sorted((a1, a2) -> Long.compare(a2.getLikesCount(), a1.getLikesCount()))
                        .collect(Collectors.toList());
                break;
            case "mostUseful":
                articles = articles.stream()
                        .sorted((a1, a2) -> Integer.compare(a2.getUseful_count(), a1.getUseful_count()))
                        .collect(Collectors.toList());
                break;
            case "mostViewed":
                articles = articles.stream()
                        .sorted((a1, a2) -> Integer.compare(a2.getViews(), a1.getViews()))
                        .collect(Collectors.toList());
                break;
            case "newest":
            default:
                articles = articles.stream()
                        .sorted((a1, a2) -> a2.getCreated_at().compareTo(a1.getCreated_at()))
                        .collect(Collectors.toList());
                break;
        }

        if (user != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String gravatarHash = md5Hex(user.getEmail());
            String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";
            model.addAttribute("user", user);
            model.addAttribute("gravatar", gravatarUrl);
        }

        String gravatarHash = md5Hex(auth.getName());
        String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";

        model.addAttribute("gravatar", gravatarUrl);
        List<Tag> allTags = tagRepository.findAll();
        model.addAttribute("allTags", allTags);
        model.addAttribute("articles", articles);
        model.addAttribute("selectedTagIds", tagIds);
        model.addAttribute("selectedSortBy", sortBy);
        model.addAttribute("user", user);
        return "general/home";
    }




    @PostMapping("/comment/add")
    public String addComment(
            @RequestParam UUID articleId,
            @RequestParam String content,
            @RequestParam(required = false) UUID parentCommentId
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepo.findUserByEmail(auth.getName());
        Article article = articleRepo.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setArticle(article);
        comment.setContent(content);

        if (parentCommentId != null) {
            Comment parent = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Comentario padre no encontrado"));
            comment.setParentComment(parent);
        }

        commentRepository.save(comment);
        return "redirect:/article/view/" + article.getUser().getName() + "/" + article.getSlug();
    }



}
