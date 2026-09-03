package me.kimyeonsup.home.domain.blog.article.domain.entity;

import lombok.extern.slf4j.Slf4j;
import me.kimyeonsup.home.domain.blog.admin.menu.domain.entity.Menu;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
class ArticleTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "![](https://example.com/images/sample-markdown-image.png)"
            ,
            "<img src=\"https://example.com/images/sample-html-image.png\">"})
    void testSetArticleThumbnail(String content) {

        Article article = Article.builder()
                .title("title")
                .subTitle("subTitle")
                .author("admin")
                .menu(Menu.builder().id(1L).build())
                .content(content)
                .visibility("PUBLIC")
                .build();

        log.info("image Url : {}", article.getThumbnailUrl());
    }
}
