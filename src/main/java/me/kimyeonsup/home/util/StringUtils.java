package me.kimyeonsup.home.util;

import java.util.regex.Pattern;

public class StringUtils {

    private static final Pattern SPECIAL_CHARACTER = Pattern.compile("[#*()\\\":{}|]");

    public static String replaceAllSpecialCharacter(String text) {
        return SPECIAL_CHARACTER.matcher(text).replaceAll("");
    }

    public static String nvl(String title, String s) {
        if (isNull(title)) {
            return s;
        }
        return title;
    }

    public static boolean isNull(String str) {
        return str == null || str.trim().isEmpty();
    }
}
