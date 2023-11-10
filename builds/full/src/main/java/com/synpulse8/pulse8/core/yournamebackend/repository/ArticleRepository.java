package com.synpulse8.pulse8.core.yournamebackend.repository;

import com.synpulse8.pulse8.core.yournamebackend.entity.Article;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ArticleRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Article> findAll() {
        return em.createQuery("select e from Article e", Article.class).getResultList();
    }

}
