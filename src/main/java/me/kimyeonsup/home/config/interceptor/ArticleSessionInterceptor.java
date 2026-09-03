package me.kimyeonsup.home.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Objects;

@Component
public class ArticleSessionInterceptor implements HandlerInterceptor {

    private static final int ARTICLES_INTERVAL_MAX = 2 * 60 * 60;
    private static final int GENERAL_INTERVAL_MAX = 30 * 60;
    private static final String[] ARTICLES_URL = {"new-article"};

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (Objects.isNull(request.getSession())) {
            return true;
        }

        String requestURL = request.getRequestURL().toString();
        if (!requestURL.contains("blog")) {
            return true;
        }

        if (isExtension(requestURL)) {
            request.getSession().setMaxInactiveInterval(ARTICLES_INTERVAL_MAX);
            return true;
        }

        request.getSession().setMaxInactiveInterval(GENERAL_INTERVAL_MAX);
//        return HandlerInterceptor.super.preHandle(request, response, handler);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    private boolean isExtension(String requestURL) {
        for (String url : ARTICLES_URL) {
            if (requestURL.contains(url)) {
                return true;
            }
        }
        return false;
    }
}
