package me.kimyeonsup.home.global.filter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class HttpLoggingFilterTest {

    private HttpLoggingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new HttpLoggingFilter();
    }

    @Test
    void passesResponseThroughWithoutCapturingBodyAndClearsRequestContext() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/articles");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (servletRequest, servletResponse) -> {
            ((HttpServletResponse) servletResponse).setStatus(200);
            servletResponse.getWriter().write("response body");
        };

        filter.doFilter(request, response, chain);

        assertThat(response.getContentAsString()).isEqualTo("response body");
        assertThat(MDC.get("clientIP")).isNull();
        assertThat(MDC.get("requestId")).isNull();
    }
}
