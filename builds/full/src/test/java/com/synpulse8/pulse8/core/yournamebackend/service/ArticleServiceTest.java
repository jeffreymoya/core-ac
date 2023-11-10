package com.synpulse8.pulse8.core.yournamebackend.service;

import com.synpulse8.pulse8.core.yournamebackend.config.auth.P8CAuthenticationContextImpl;
import com.synpulse8.pulse8.core.yournamebackend.kafka.producer.KafkaProducerService;
import com.synpulse8.pulse8.core.yournamebackend.repository.ArticleJpaRepository;
import com.synpulse8.pulse8.core.yournamebackend.repository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @InjectMocks
    private ArticleService articleService;

    @Mock
    private P8CAuthenticationContextImpl authContext;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private ArticleJpaRepository jpaRepo;

    @Mock
    private KafkaProducerService kafkaProducerService;



    @BeforeEach
    void setUp() {
        // block for code running before each test
    }

    @Test
    void getAllArticles() {
        articleService.getAllArticles();
        verify(articleRepository).findAll();
    }

    @Test
    void fetchById() {
        articleService.fetchById(1);
        verify(jpaRepo).findById(1);
    }


}
