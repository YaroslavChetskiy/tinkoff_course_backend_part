package edu.java.bot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.java.bot.model.dto.request.LinkUpdateRequest;
import edu.java.bot.service.BotService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class UpdateControllerTest {

    private static final String URL = "/updates";

    @MockBean
    private BotService botService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        this.objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Отправка корректного обновления")
    void shouldReturnOkAfterSendCorrectUpdate() throws Exception {
        var linkUpdateRequest = new LinkUpdateRequest(1L, "dummy.com", "dummy", List.of(1L, 2L));

        doNothing().when(botService).sendUpdate(linkUpdateRequest);

        String requestJson = objectMapper.writeValueAsString(linkUpdateRequest);

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(content().string("Обновление обработано"));

        Mockito.verify(botService, Mockito.times(1)).sendUpdate(linkUpdateRequest);
    }

    @Test
    @DisplayName("Отправка корректного обновления")
    void shouldReturnBadRequestAfterSendInvalidUpdate() throws Exception {

        mockMvc.perform(post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "id": 123,
                      "url": "",
                      "description": "Пример описания",
                      "tgChatIds": []
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"));

    }

}
