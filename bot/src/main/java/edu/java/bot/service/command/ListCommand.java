package edu.java.bot.service.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.model.entity.Link;
import edu.java.bot.service.messageProcessor.UserMessageProcessor;
import edu.java.bot.service.storage.UserStorage;

public class ListCommand extends BaseCommand {

    private static final String COMMAND = "/list";
    private static final String DESCRIPTION = "Показать список отслеживаемых ссылок.";

    private static final String EMPTY_LINK_LIST = "Вы не отслеживаете ни одной ссылки.";

    public ListCommand(UserMessageProcessor processor, UserStorage storage) {
        super(processor, storage);
    }

    @Override
    public SendMessage handle(Update update) {
        var id = update.message().chat().id();
        var user = storage.findById(id);
        if (user.isPresent()) {
            var links = user.get().getLinks();
            if (!links.isEmpty()) {
                StringBuilder stringBuilder = new StringBuilder("Список отслеживаемых ссылок:\n");
                for (Link link : links) {
                    stringBuilder.append(" - ").append(link.toString()).append("\n");
                }
                return new SendMessage(id, stringBuilder.toString()).disableWebPagePreview(true);
            }
        }
        return new SendMessage(id, EMPTY_LINK_LIST);
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
