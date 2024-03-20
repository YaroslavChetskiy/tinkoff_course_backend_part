package edu.java.service.jooq;

import edu.java.domain.repository.jooq.JooqChatRepository;
import edu.java.dto.entity.jdbc.Chat;
import edu.java.exception.ChatAlreadyRegisteredException;
import edu.java.exception.ChatNotFoundException;
import edu.java.service.ChatService;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

// честно не знаю, что может поменяться, кроме как реализации chatRepository,
// но раз в задании говорят, что нужно делать разные реализации сервисов, меняя
// реализацию репозиториев, то сделаю

@RequiredArgsConstructor
public class JooqChatService implements ChatService {

    private final JooqChatRepository chatRepository;

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
