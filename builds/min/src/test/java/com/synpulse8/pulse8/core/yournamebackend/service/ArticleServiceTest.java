package com.synpulse8.pulse8.core.yournamebackend.service;

import com.synpulse8.pulse8.core.yournamebackend.config.auth.P8CAuthenticationContextImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @InjectMocks
    private ArticleService articleService;

    @Mock
    private P8CAuthenticationContextImpl authContext;



    @BeforeEach
    void setUp() {
        // block for code running before each test
    }

    @Test
    void getAllArticles() {
        assertThat(articleService.getAllArticles()).isNotEmpty();
    }

    @Test
    void fetchById() {
        assertThat(articleService.fetchById(1)).isNotNull();
    }


}
