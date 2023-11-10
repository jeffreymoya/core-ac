package com.synpulse8.pulse8.core.yournamebackend.controller;

import com.synpulse8.pulse8.core.yournamebackend.entity.Article;
import com.synpulse8.pulse8.core.yournamebackend.service.ArticleService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping(value = "/article")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<Article> getAllArticles() {
        return this.articleService.getAllArticles();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    Article getArticleById(@PathVariable("id") Integer id) {
        return this.articleService.fetchById(id);
    }

}
