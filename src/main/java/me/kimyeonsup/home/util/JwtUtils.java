package me.kimyeonsup.home.util;

public class JwtUtils {

    private static final String TOKEN_PREFIX = "Bearer ";

    public static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
            return null;
        }

        return authorizationHeader.substring(TOKEN_PREFIX.length());
    }
}
