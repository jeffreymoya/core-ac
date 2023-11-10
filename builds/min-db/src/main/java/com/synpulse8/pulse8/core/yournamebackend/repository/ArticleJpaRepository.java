package com.synpulse8.pulse8.core.yournamebackend.repository;

import com.synpulse8.pulse8.core.yournamebackend.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleJpaRepository extends JpaRepository<Article, Integer> {

    Optional<Article> findById(Integer id);

}
