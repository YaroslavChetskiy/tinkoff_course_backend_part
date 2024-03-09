package edu.java.domain.repository;

import edu.java.dto.entity.Chat;

public interface ChatRepository {

    void saveChat(Chat chat);

    void deleteChat(Long chatId);

    Chat findById(Long chatId);
}
