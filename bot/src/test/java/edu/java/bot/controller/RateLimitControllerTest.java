package edu.java.bot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.filter.servlet.ServletRequestFilter;
import edu.java.bot.model.dto.request.LinkUpdateRequest;
import edu.java.bot.service.BotService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext // пришлось добавить, так как почему-то пытается создаться два раза JCacheCacheManager
// Это, как я понял, баг, нормального решения которого я, к сожалению, найти не смог
public class RateLimitControllerTest {

    private static final String URL = "/updates";

    private static final LinkUpdateRequest REQUEST = new LinkUpdateRequest(
        1L,
        "dummy.com",
        "dummy",
        List.of(1L, 2L)
    );

    @MockBean
    private BotService botService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private GenericApplicationContext context;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        // бин создаётся в run-time, поэтому приходится брать фильтр так :)
        this.objectMapper = new ObjectMapper();
        var bean = context.getBean(ServletRequestFilter.class);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).addFilter(bean).build();
        doNothing().when(botService).sendUpdate(REQUEST);
        cacheManager.getCache("buckets").clear();
    }

    @Test
    void shouldReturnTooManyRequestsStatusAfterReachingRateLimitInUpdateController() throws Exception {
        String requestJson = objectMapper.writeValueAsString(REQUEST);
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isOk());
        }

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isTooManyRequests());
    }
}
