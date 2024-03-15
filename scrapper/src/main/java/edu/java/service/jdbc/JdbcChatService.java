package edu.java.service.jdbc;

import edu.java.domain.repository.jdbc.JdbcChatRepository;
import edu.java.dto.entity.Chat;
import edu.java.exception.ChatAlreadyRegisteredException;
import edu.java.exception.ChatNotFoundException;
import edu.java.service.ChatService;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JdbcChatService implements ChatService {

    private final JdbcChatRepository chatRepository;

    @Transactional
    public void registerChat(Long chatId) {
        var chat = chatRepository.findById(chatId);

        if (chat != null) {
            throw new ChatAlreadyRegisteredException("Чат уже зарегистрирован");
        }

        chatRepository.saveChat(new Chat(chatId, OffsetDateTime.now()));
    }

    @Transactional
    public void deleteChat(Long chatId) {
        var chat = chatRepository.findById(chatId);

        if (chat == null) {
            throw new ChatNotFoundException("Чат не найден");
        }

        chatRepository.deleteChat(chatId);
    }
}
