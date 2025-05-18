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
        model.addAttribute("collections", collectionRepo.findByUser(user));
        model.addAttribute("article", new Article());
        model.addAttribute("allTags", tagRepository.findAll());
        return "article/create";
    }

    @GetMapping("/view/{name}/{articleSlug}")
    public String viewArticle(
            @PathVariable String name,
            @PathVariable String articleSlug,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) {

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

        boolean shouldCountView = false;

        if (viewer != null && !viewer.equals(article.getUser())) {
            // Usuario autenticado: revisa última vista para evitar duplicados
            Optional<ArticleView> lastView = ViewRepo.findTopByUserAndArticleOrderByViewedAtDesc(viewer, article);

            shouldCountView = lastView.isEmpty() ||
                    lastView.get().getViewedAt().isBefore(LocalDateTime.now().minusMinutes(1));

            if (shouldCountView) {
                ArticleView av = new ArticleView();
                av.setArticle(article);
                av.setUser(viewer);
                av.setViewedAt(LocalDateTime.now());
                articleViewRepository.save(av);
            }
        } else if (visitorId != null) {
            // Usuario anónimo: cuenta vistas por visitorId
            Optional<ArticleView> lastView = ViewRepo.findTopByVisitorIdAndArticleOrderByViewedAtDesc(visitorId, article);

            shouldCountView = lastView.isEmpty() ||
                    lastView.get().getViewedAt().isBefore(LocalDateTime.now().minusMinutes(1));

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

        // Asignar tags si hay
        if (tagIds != null && !tagIds.isEmpty()) {
            Set<Tag> tags = new HashSet<>(tagRepository.findAllById(tagIds));
            article.setTags(tags);
        } else {
            article.setTags(new HashSet<>());  // o null si prefieres
        }

        // Manejo del slug
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

        model.addAttribute("articles", articles);
        model.addAttribute("commentsMap", commentsMap);
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

        model.addAttribute("article", article);
        model.addAttribute("collections", collectionRepo.findByUser(user));
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
                       Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = null;

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            user = userRepo.findUserByEmail(auth.getName());
        }

        List<Article> articles;
        if (tagIds != null && !tagIds.isEmpty()) {
            articles = articleRepo.findByTags_IdIn(tagIds);
        } else {
            articles = articleRepo.findAll();
        }

        if (user != null) {
            String gravatarHash = md5Hex(user.getEmail());
            String gravatarUrl = "https://www.gravatar.com/avatar/" + gravatarHash + "?s=100&d=identicon";
            model.addAttribute("user", user);
            model.addAttribute("gravatarUrl", gravatarUrl);
        }

        List<Tag> allTags = tagRepository.findAll();
        model.addAttribute("allTags", allTags);
        model.addAttribute("articles", articles);
        model.addAttribute("selectedTagIds", tagIds);
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
