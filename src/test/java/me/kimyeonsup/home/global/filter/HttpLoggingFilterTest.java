package me.kimyeonsup.home.global.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class HttpLoggingFilterTest {

    @Test
    @DisplayName("API 응답 본문을 로그에 남기지 않는다")
    void doesNotLogApiResponseBody() throws ServletException, IOException {
        HttpLoggingFilter filter = new HttpLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String sensitiveValue = "signed-url-or-personal-data";
        Logger logger = (Logger) LoggerFactory.getLogger(HttpLoggingFilter.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);

        try {
            filter.doFilter(request, response,
                    (req, res) -> res.getWriter().write("{\"data\":\"" + sensitiveValue + "\"}"));

            assertThat(appender.list)
                    .extracting(ILoggingEvent::getFormattedMessage)
                    .noneMatch(message -> message.contains(sensitiveValue));
        } finally {
            logger.detachAppender(appender);
            appender.stop();
        }
    }

    @Test
    @DisplayName("필터 처리 중 예외가 발생해도 MDC를 정리한다")
    void clearsMdcWhenFilterChainThrows() {
        HttpLoggingFilter filter = new HttpLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/articles");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response,
                (req, res) -> { throw new ServletException("test"); }))
                .isInstanceOf(ServletException.class);

        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }
}
