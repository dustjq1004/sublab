package me.kimyeonsup.home.domain.blog.article.domain.vo;

import me.kimyeonsup.home.global.exception.business.RequiredValueException;

public enum Visibility {
    PUBLIC,
    PRIVATE;

    public static Visibility from(String visibility) {
        for (Visibility v : Visibility.values()) {
            if (v.name().equalsIgnoreCase(visibility)) {
                return v;
            }
        }
        throw new RequiredValueException("visibility", "%s 값이 올바르지 않습니다.");
    }
}
