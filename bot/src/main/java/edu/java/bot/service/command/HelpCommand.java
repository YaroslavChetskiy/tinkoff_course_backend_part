package edu.java.bot.service.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.service.messageProcessor.UserMessageProcessor;
import edu.java.bot.service.storage.UserStorage;

public class HelpCommand extends BaseCommand {

    private static final String COMMAND = "/help";
    private static final String DESCRIPTION = "Показать список доступных команд.";

    public HelpCommand(UserMessageProcessor processor, UserStorage storage) {
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
        StringBuilder stringBuilder = new StringBuilder("Список команд:\n");
        for (Command command : processor.commands()) {
            stringBuilder.append("- ").append(command).append("\n");
        }
        return new SendMessage(update.message().chat().id(), stringBuilder.toString());
    }
}
