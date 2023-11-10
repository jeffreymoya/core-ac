package com.synpulse8.pulse8.core.yournamebackend.service;

import com.synpulse8.pulse8.core.yournamebackend.config.auth.P8CAuthenticationContextImpl;
import com.synpulse8.pulse8.core.yournamebackend.entity.Article;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Arrays;

@Log4j2
@Service
public class ArticleService {

    private final P8CAuthenticationContextImpl authContext;


    public static final String PROCESS_ARTICLES_MESSAGE = "Article test value";

    public ArticleService(P8CAuthenticationContextImpl authContext) {
        this.authContext = authContext;
    }


    public List<Article> articles = Arrays.asList(new Article(1, "First article"), new Article(2, "Second article"), new Article(3, "Third article"));

    public List<Article> getAllArticles() {
        if (this.authContext.isAdmin()) {
            log.info("Retrieving articles for user with admin role");
        } else {
            log.info("Retrieving articles for user without admin role");
        }

        String username = this.authContext.getUsername();
        List<String> groups = this.authContext.getUserGroups();
        List<String> hierarchies = this.authContext.getUserHierarchies();
        log.info("Logged in user {} from groups {} and group hierarchies {}", username, groups, hierarchies);

        return this.articles;
    }

    public Article fetchById(Integer id) {
        return this.articles.stream().filter(article -> article.getId().equals(id)).findFirst().orElse(null);
    }


}
