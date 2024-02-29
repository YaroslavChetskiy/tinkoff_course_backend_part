package edu.java.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.exception.LinkAlreadyTrackedException;
import edu.java.exception.LinkNotFoundException;
import edu.java.service.LinkService;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LinkController.class)
class LinkControllerTest {

    private static final String CHAT_ID_HEADER = "Tg-Chat-Id";
    private static final String LINKS_URL = "/links";

    private static final Long CHAT_ID = 1L;

    @MockBean
    private LinkService linkService;

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
    @DisplayName("Получение списка всех ссылок")
    void shouldReturnOkAfterGetAllLinks() throws Exception {
        var expectedResult = new ListLinksResponse(
            List.of(new LinkResponse(1L, "dummy.com")),
            1
        );

        when(linkService.getAllLinks(CHAT_ID)).thenReturn(expectedResult);

        mockMvc.perform(get(LINKS_URL)
                .header(CHAT_ID_HEADER, CHAT_ID))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.links[0].id").value(expectedResult.links().getFirst().id()))
            .andExpect(jsonPath("$.links[0].url").value(expectedResult.links().getFirst().url()))
            .andExpect(jsonPath("$.size").value(expectedResult.size()));

        Mockito.verify(linkService, Mockito.times(1)).getAllLinks(CHAT_ID);
    }

    @Test
    @DisplayName("Добавление корректной ссылки")
    void shouldReturnOkIfAddRequestIsCorrect() throws Exception {
        var addLinkRequest = new AddLinkRequest("dummy.com");
        var expectedResult = new LinkResponse(1L, "dummy.com");
        when(linkService.addLink(CHAT_ID, addLinkRequest)).thenReturn(expectedResult);

        String requestJson = objectMapper.writeValueAsString(addLinkRequest);

        mockMvc.perform(post(LINKS_URL)
                .header(CHAT_ID_HEADER, CHAT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedResult.id()))
            .andExpect(jsonPath("$.url").value(expectedResult.url()));

        Mockito.verify(linkService, Mockito.times(1)).addLink(CHAT_ID, addLinkRequest);

    }

    @Test
    @DisplayName("Удаление корректной ссылки")
    void shouldReturnOkIfRemoveRequestIsCorrect() throws Exception {
        var removeLinkRequest = new RemoveLinkRequest("dummy.com");
        var expectedResult = new LinkResponse(1L, "dummy.com");
        when(linkService.removeLink(CHAT_ID, removeLinkRequest)).thenReturn(expectedResult);

        String requestJson = objectMapper.writeValueAsString(removeLinkRequest);

        mockMvc.perform(delete(LINKS_URL)
                .header(CHAT_ID_HEADER, CHAT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(expectedResult.id()))
            .andExpect(jsonPath("$.url").value(expectedResult.url()));

        Mockito.verify(linkService, Mockito.times(1)).removeLink(CHAT_ID, removeLinkRequest);
    }

    @Test
    @DisplayName("Добавление уже существующей ссылки")
    void shouldReturnConflictIfLinkAlreadyTracked() throws Exception {
        var addLinkRequest = new AddLinkRequest("dummy.com");
        when(linkService.addLink(CHAT_ID, addLinkRequest)).thenThrow(LinkAlreadyTrackedException.class);

        String requestJson = objectMapper.writeValueAsString(addLinkRequest);

        mockMvc.perform(post(LINKS_URL)
                .header(CHAT_ID_HEADER, CHAT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.description").value("Ссылка уже отслеживается"));

        Mockito.verify(linkService, Mockito.times(1)).addLink(CHAT_ID, addLinkRequest);
    }

    @Test
    @DisplayName("Удаление не отслеживаемой ссылки")
    void shouldReturnNotFoundIfLinkNotTracked() throws Exception {
        var removeLinkRequest = new RemoveLinkRequest("dummy.com");
        when(linkService.removeLink(CHAT_ID, removeLinkRequest)).thenThrow(LinkNotFoundException.class);

        String requestJson = objectMapper.writeValueAsString(removeLinkRequest);

        mockMvc.perform(delete(LINKS_URL)
                .header(CHAT_ID_HEADER, CHAT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.description").value("Ссылка не найдена"));

        Mockito.verify(linkService, Mockito.times(1)).removeLink(CHAT_ID, removeLinkRequest);
    }

    @Test
    @DisplayName("Отправка невалидного запроса")
    void shouldReturnBadRequestIfRequestIsNotCorrect() throws Exception {
        mockMvc.perform(delete(LINKS_URL)
                .header(CHAT_ID_HEADER, CHAT_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "link": ""
                    }
                        """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.description").value("Некорректные параметры запроса"));

    }
}
