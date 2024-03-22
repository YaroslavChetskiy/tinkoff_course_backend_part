package edu.java.service;

import edu.java.domain.repository.jdbc.JdbcChatRepository;
import edu.java.dto.entity.jdbc.Chat;
import edu.java.exception.ChatAlreadyRegisteredException;
import edu.java.exception.ChatNotFoundException;
import edu.java.service.jdbc.JdbcChatService;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JdbcChatServiceTest {

    private static final Chat CHAT = new Chat(1L, OffsetDateTime.now());
    private static final long NOT_EXISTING_CHAT_ID = 2L;
    @Mock
    private JdbcChatRepository chatRepository;

    @InjectMocks
    private JdbcChatService chatService;

    @Test
    void registerChat() {
        when(chatRepository.findById(CHAT.getId())).thenReturn(CHAT);
        when(chatRepository.findById(NOT_EXISTING_CHAT_ID)).thenReturn(null);

        assertThrows(ChatAlreadyRegisteredException.class, () -> chatService.registerChat(CHAT.getId()));

        chatService.registerChat(NOT_EXISTING_CHAT_ID);

        verify(chatRepository, times(1)).saveChat(any(Chat.class));
    }

    @Test
    void deleteChat() {
        when(chatRepository.findById(CHAT.getId())).thenReturn(CHAT);
        when(chatRepository.findById(NOT_EXISTING_CHAT_ID)).thenReturn(null);

        assertThrows(ChatNotFoundException.class, () -> chatService.deleteChat(NOT_EXISTING_CHAT_ID));

        chatService.deleteChat(CHAT.getId());

        verify(chatRepository, times(1)).deleteChat(CHAT.getId());
    }
}
