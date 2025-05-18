package com.cyberlog.Controllers.Tag;

import com.cyberlog.Models.Article;
import com.cyberlog.Models.Tag;
import com.cyberlog.Repositories.Article.ArticleRepositoryCRUD;
import org.springframework.ui.Model;
import com.cyberlog.Repositories.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tag")
public class TagController {

    @Autowired
    private TagRepository tagRepo;

    @Autowired
    private ArticleRepositoryCRUD articleRepo;

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("tag", new Tag());
        model.addAttribute("tags", tagRepo.findAll());
        return "tag/create";
    }

    @PostMapping("/create")
    public String create(Tag tag, Model model) {
        tagRepo.save(tag);
        model.addAttribute("tag", new Tag());
        model.addAttribute("tags", tagRepo.findAll());
        return "tag/create";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam("id") UUID tag, Model model) {
        tagRepo.deleteById(tag);
        model.addAttribute("tag", new Tag());
        model.addAttribute("tags", tagRepo.findAll());
        return "tag/create";
    }

    public void assignTagsToArticle(UUID articleId, List<UUID> tagIds) {
        Article article = articleRepo.findById(articleId).orElseThrow(() -> new RuntimeException("Artículo no encontrado"));

        Set<Tag> tags = new HashSet<>();
        for (UUID tagId : tagIds) {
            Tag tag = tagRepo.findById(tagId).orElseThrow(() -> new RuntimeException("Tag no encontrado: " + tagId));
            tags.add(tag);
        }

        article.getTags().clear();  // si quieres reemplazar los tags actuales
        article.getTags().addAll(tags);

        articleRepo.save(article);
    }


    @PostMapping("/assign-tags")
    public String assignTags(@RequestParam UUID articleId, @RequestParam List<UUID> tagIds) {
        assignTagsToArticle(articleId, tagIds);
        return "redirect:/article/edit?id=" + articleId;
    }


}
