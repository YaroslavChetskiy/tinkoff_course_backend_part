package edu.java.domain.repository;

import edu.java.dto.entity.jdbc.Chat;

public interface ChatRepository {

    Chat saveChat(Chat chat);

    Chat deleteChat(Long chatId);

    Chat findById(Long chatId);
}
