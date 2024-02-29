package edu.java.bot.service;

import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.Bot;
import edu.java.bot.model.dto.request.LinkUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BotService {

    private final Bot bot;

    public BotService(Bot bot) {
        this.bot = bot;
    }

    public void sendUpdate(LinkUpdateRequest updateRequest) {
        StringBuilder stringBuilder = new StringBuilder("Пришло обновление!\n");
        stringBuilder.append(updateRequest.id())
            .append(": ")
            .append(updateRequest.url())
            .append("\nОписание:\n")
            .append(updateRequest.description());

        for (Long chatId : updateRequest.tgChatIds()) {
            bot.execute(new SendMessage(chatId, stringBuilder.toString()));
        }
    }
}
