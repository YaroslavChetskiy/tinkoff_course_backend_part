package edu.java.controller;

import com.giffing.bucket4j.spring.boot.starter.filter.servlet.ServletRequestFilter;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.service.ChatService;
import edu.java.service.LinkService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext // пришлось добавить, так как почему-то пытается создаться два раза JCacheCacheManager
// Это, как я понял, баг, нормального решения которого я, к сожалению, найти не смог
public class RateLimitControllerTest {

    private static final String CHAT_URL = "/tg-chat/";

    private static final String CHAT_ID_HEADER = "Tg-Chat-Id";

    private static final String LINKS_URL = "/links";

    private static final Long CHAT_ID = 1L;

    private static final ListLinksResponse RESPONSE = new ListLinksResponse(
        List.of(new LinkResponse(1L, "dummy.com")),
        1
    );

    @MockBean
    private ChatService chatService;

    @MockBean
    private LinkService linkService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private GenericApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        // бин создаётся в run-time, поэтому приходится брать фильтр так :)
        var bean = context.getBean(ServletRequestFilter.class);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).addFilter(bean).build();
        doNothing().when(chatService).registerChat(anyLong());
        when(linkService.getAllLinks(CHAT_ID)).thenReturn(RESPONSE);
        cacheManager.getCache("buckets").clear();
    }

    @Test
    void shouldReturnTooManyRequestsStatusAfterReachingRateLimitInChatController() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post(CHAT_URL + CHAT_ID))
                .andExpect(status().isOk());
        }

        mockMvc.perform(post(CHAT_URL + CHAT_ID))
            .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldReturnTooManyRequestsStatusAfterReachingRateLimitInLinkController() throws Exception {
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get(LINKS_URL).header(CHAT_ID_HEADER, CHAT_ID))
                .andExpect(status().isOk());
        }

        mockMvc.perform(get(LINKS_URL).header(CHAT_ID_HEADER, CHAT_ID))
            .andExpect(status().isTooManyRequests());
    }
}
