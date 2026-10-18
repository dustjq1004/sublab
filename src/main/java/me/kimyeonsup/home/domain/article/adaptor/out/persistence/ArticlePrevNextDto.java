package me.kimyeonsup.home.domain.article.adaptor.out.persistence;


public interface ArticlePrevNextDto {

    Long getId();

    Long getPrevId();

    String getPrevTitle();

    Long getNextId();

    String getNextTitle();
}
