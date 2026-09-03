package me.kimyeonsup.home.domain.main.domain.dto.title;

import lombok.Getter;

@Getter
public enum TitleWords {

    IT("IT"),
    LANGUAGE("LANGUAGE"),
    BLOG("BLOG"),
    DOCS("DOCS"),
    WORKOUT("WORKOUT"),
    PROJECTS("PROJECTS");

    private String title;

    TitleWords(String title) {
        this.title = title;
    }
}
