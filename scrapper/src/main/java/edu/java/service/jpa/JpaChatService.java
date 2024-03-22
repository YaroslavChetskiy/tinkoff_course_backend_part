package edu.java.service.jpa;

import edu.java.domain.repository.jpa.JpaChatRepository;
import edu.java.dto.entity.hibernate.Chat;
import edu.java.exception.ChatAlreadyRegisteredException;
import edu.java.exception.ChatNotFoundException;
import edu.java.service.ChatService;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class JpaChatService implements ChatService {

    private final JpaChatRepository chatRepository;

    // на самом деле не уверен, как можно сделать не check and save, так как
    // метод save не выбрасывает исключений при сохранении дубликата (просто обновляет существующий, как я понимаю)
    @Transactional
    @Override
    public void registerChat(Long chatId) {
        if (chatRepository.existsById(chatId)) {
            throw new ChatAlreadyRegisteredException("Чат уже зарегистрирован");
        }
        chatRepository.save(Chat.builder()
            .id(chatId)
            .createdAt(OffsetDateTime.now())
            .build());
    }

    @Transactional
    @Override
    public void deleteChat(Long chatId) {
        chatRepository.findById(chatId)
            .ifPresentOrElse(
                entity -> {
                    chatRepository.delete(entity);
                    chatRepository.flush();
                },
                () -> {
                    throw new ChatNotFoundException("Чат не найден");
                }
            );
    }
}
