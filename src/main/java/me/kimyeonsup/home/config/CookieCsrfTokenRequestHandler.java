package me.kimyeonsup.home.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

public class CookieCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       Supplier<CsrfToken> csrfToken) {
        super.handle(request, response, csrfToken);
        csrfToken.get();
    }
}
