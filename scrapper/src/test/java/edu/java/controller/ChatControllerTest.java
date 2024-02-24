package edu.java.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.java.exception.ChatAlreadyRegisteredException;
import edu.java.exception.ChatNotFoundException;
import edu.java.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    private static final String CHAT_URL = "/tg-chat/";

    private static final Long CHAT_ID = 1L;

    @MockBean
    private ChatService chatService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @DisplayName("Регистрация незарегистрированного чата")
    void shouldReturnOkAfterRegisterChat() throws Exception {
        doNothing().when(chatService).registerChat(CHAT_ID);

        mockMvc.perform(post(CHAT_URL + CHAT_ID))
            .andExpect(status().isOk())
            .andExpect(content().string("Чат зарегистрирован"));
    }

    @Test
    @DisplayName("Регистрация незарегистрированного чата")
    void shouldReturnOkAfterDeleteExistedChat() throws Exception {
        doNothing().when(chatService).deleteChat(CHAT_ID);

        mockMvc.perform(delete(CHAT_URL + CHAT_ID))
            .andExpect(status().isOk())
            .andExpect(content().string("Чат успешно удалён"));
    }

    @Test
    @DisplayName("Получение ответа об повторной регистрации")
    void shouldReturnConflictAfterReRegistration() throws Exception {
        doThrow(ChatAlreadyRegisteredException.class).when(chatService).registerChat(CHAT_ID);

        mockMvc.perform(post(CHAT_URL + CHAT_ID))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.description").value("Чат уже зарегистрирован"));
    }

    @Test
    @DisplayName("Получение ответа об повторной регистрации")
    void shouldReturnNotFoundAfterDeletingNotExistedChat() throws Exception {
        doThrow(ChatNotFoundException.class).when(chatService).deleteChat(CHAT_ID);

        mockMvc.perform(delete(CHAT_URL + CHAT_ID))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.description").value("Чат не найден"));
    }
}
