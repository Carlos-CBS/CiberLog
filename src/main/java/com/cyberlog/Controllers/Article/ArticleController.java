package com.cyberlog.Controllers.Article;

import com.cyberlog.Models.Article;
import com.cyberlog.Repositories.Article.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepo;

    @GetMapping("/create")
    public String ViewCreate(){
        return "article/create";
    }

    @GetMapping("/update")
    public String ViewUpdate(){
        return "article/update";
    }

    @PostMapping("/create")
    public String ViewCreate(@ModelAttribute Article article){
        articleRepo.save(article);
        return "redirect:/article";
    }

}