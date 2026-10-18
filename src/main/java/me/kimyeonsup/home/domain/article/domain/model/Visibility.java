package me.kimyeonsup.home.domain.article.domain.model;

public enum Visibility {
    PUBLIC,
    PRIVATE;

    public static Visibility from(String visibility) {
        for (Visibility v : Visibility.values()) {
            if (v.name().equalsIgnoreCase(visibility)) {
                return v;
            }
        }
        return null;
    }
}
