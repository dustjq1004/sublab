package me.kimyeonsup.home.config.interceptor;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
@SpringBootTest
@AutoConfigureMockMvc
class ArticleSessionInterceptorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "dustjq")
    public void testNewArticleSessionTimeout() throws Exception {
        mockMvc.perform(get("/blog/new-article"))
                .andExpect(status().isOk())
                .andDo(result -> {
                    HttpSession session = result.getRequest().getSession();
                    assert session != null;
                    int maxInactiveInterval = session.getMaxInactiveInterval();
                    // Verify the session timeout is set to 2 hours (7200 seconds)
                    assertThat(maxInactiveInterval).isEqualTo(2 * 60 * 60);
                });
    }
}
