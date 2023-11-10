package com.synpulse8.pulse8.core.yournamebackend.service;

import com.synpulse8.pulse8.core.yournamebackend.config.auth.P8CAuthenticationContextImpl;
import com.synpulse8.pulse8.core.yournamebackend.entity.Article;
import com.synpulse8.pulse8.core.yournamebackend.repository.ArticleJpaRepository;
import com.synpulse8.pulse8.core.yournamebackend.repository.ArticleRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@Service
public class ArticleService {

    private final P8CAuthenticationContextImpl authContext;
    private final ArticleRepository repo;
    private final ArticleJpaRepository jpaRepo;


    public static final String PROCESS_ARTICLES_MESSAGE = "Article test value";

    public ArticleService(P8CAuthenticationContextImpl authContext,
                          ArticleRepository repo,
                          ArticleJpaRepository jpaRepo) {
        this.authContext = authContext;
        this.repo = repo;
        this.jpaRepo = jpaRepo;
    }


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

        return repo.findAll();
    }

    public Article fetchById(Integer id) {
        return this.jpaRepo.findById(id).orElse(null);
    }


}
