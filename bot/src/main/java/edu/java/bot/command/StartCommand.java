package edu.java.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.bot.messageProcessor.UserMessageProcessor;

public class StartCommand extends BaseCommand {

    private static final String COMMAND = "/start";
    private static final String DESCRIPTION = "Начать работу.";

    private static final String START_MESSAGE = "Бот запущен! Он поможет вам с отслеживанием ссылок.";
    private static final String REPEATED_REGISTRATION_MESSAGE = "Бот уже запущен.";

    public StartCommand(UserMessageProcessor processor, ScrapperClient scrapperClient) {
        super(processor, scrapperClient);
    }

    @Override
    public SendMessage handle(Update update) {
        Long chatId = update.message().chat().id();
        String response = scrapperClient.registerChat(chatId);
        if (response == null) {
            return new SendMessage(chatId, REPEATED_REGISTRATION_MESSAGE);
        }
        return new SendMessage(chatId, START_MESSAGE);
    }

    @Override
    public String command() {
        return COMMAND;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

}
