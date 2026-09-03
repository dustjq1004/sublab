package me.kimyeonsup.home.domain.blog.article.domain.dto;


public interface ArticlePrevNextDto {

    Long getId();

    Long getPrevId();

    String getPrevTitle();

    Long getNextId();

    String getNextTitle();
}
