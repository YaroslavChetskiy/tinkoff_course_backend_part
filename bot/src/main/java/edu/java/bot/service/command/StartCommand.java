package edu.java.bot.service.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.service.messageProcessor.UserMessageProcessor;
import edu.java.bot.service.storage.UserStorage;

public class StartCommand extends BaseCommand {

    private static final String COMMAND = "/start";
    private static final String DESCRIPTION = "Начать работу.";

    private static final String START_MESSAGE = "Бот запущен! Он поможет вам с отслеживанием ссылок.";

    public StartCommand(UserMessageProcessor processor, UserStorage storage) {
        super(processor, storage);
    }

    @Override
    public String command() {
        return COMMAND;
    }

    @Override
    public String description() {
        return DESCRIPTION;
    }

    @Override
    public SendMessage handle(Update update) {
        return new SendMessage(update.message().chat().id(), START_MESSAGE);
    }

}
