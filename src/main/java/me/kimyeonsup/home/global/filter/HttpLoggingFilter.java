package me.kimyeonsup.home.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import me.kimyeonsup.home.util.StringUtils;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter extends OncePerRequestFilter {

    @Override
    public void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain filterChain) throws ServletException, IOException {
        String clientIP = request.getHeader("X-Forwarded-For");
        String requestId = request.getHeader("X-Request-Id");

        clientIP = StringUtils.nvl(clientIP, request.getRemoteAddr());
        requestId = StringUtils.nvl(requestId, UUID.randomUUID().toString()); // local 환경에서 구분을 위하여 추가

        MDC.put("clientIP", clientIP);
        MDC.put("requestId", requestId);
        
        try {
            filterChain.doFilter(request, response);

            int status = response.getStatus();
            log.debug("Request completed: {} {} ({})", request.getMethod(), request.getRequestURI(), status);
        } finally {
            MDC.clear();
        }
    }

}
