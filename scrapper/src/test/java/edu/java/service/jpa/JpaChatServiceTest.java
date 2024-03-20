package edu.java.service.jpa;

import edu.java.domain.repository.jpa.JpaChatRepository;
import edu.java.dto.entity.hibernate.Chat;
import edu.java.exception.ChatAlreadyRegisteredException;
import edu.java.exception.ChatNotFoundException;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaChatServiceTest {

    private static final Chat CHAT = Chat.builder()
        .id(255L)
        .createdAt(OffsetDateTime.now())
        .build();

    private static final Chat SECOND_CHAT = Chat.builder()
        .id(100L)
        .createdAt(OffsetDateTime.now())
        .build();

    @Mock
    private JpaChatRepository chatRepository;

    @InjectMocks
    private JpaChatService chatService;

    @Test
    void registerChat() {
        when(chatRepository.existsById(CHAT.getId())).thenReturn(true);

        assertThrows(ChatAlreadyRegisteredException.class, () -> chatService.registerChat(CHAT.getId()));

        chatService.registerChat(SECOND_CHAT.getId());

        verify(chatRepository, times(1)).save(any(Chat.class));
    }

    @Test
    void deleteChat() {
        when(chatRepository.findById(CHAT.getId())).thenReturn(Optional.empty());
        when(chatRepository.findById(SECOND_CHAT.getId())).thenReturn(Optional.of(SECOND_CHAT));

        assertThrows(ChatNotFoundException.class, () -> chatService.deleteChat(CHAT.getId()));

        chatService.deleteChat(SECOND_CHAT.getId());

        verify(chatRepository, times(1)).delete(SECOND_CHAT);
    }
}
