package edu.java.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatService {

    public void registerChat(Long chatId) {
        log.info("Чат зарегистрирован");
    }

    public void deleteChat(Long chatId) {
        log.info("Чат удалён");
    }
}
