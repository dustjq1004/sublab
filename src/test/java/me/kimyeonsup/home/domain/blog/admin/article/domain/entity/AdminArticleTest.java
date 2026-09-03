package me.kimyeonsup.home.domain.blog.admin.article.domain.entity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AdminArticleTest {


    @Test
    @DisplayName("AdminArticle 엔티티의 기본 생성자 테스트")
    void testAdminArticleDefaultConstructor() {
        // given
        AdminArticle adminArticle = new AdminArticle();

        // when
        // then
        assertNotNull(adminArticle);
    }

}