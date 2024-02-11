package edu.java.bot.service.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.service.messageProcessor.UserMessageProcessor;
import edu.java.bot.service.storage.UserStorage;
import edu.java.bot.util.LinkUtil;
import java.net.URI;
import java.net.URISyntaxException;

public class UntrackCommand extends BaseCommand {

    private static final String COMMAND = "/untrack";
    private static final String DESCRIPTION = "Прекратить отслеживание ссылки.";

    private static final String UNTRACKED_LINK = "Вы не отслеживаете данную ссылку.";

    private static final String SUCCESS_UNTRACKING = "Отслеживание ссылки прекращено.";

    public UntrackCommand(UserMessageProcessor processor, UserStorage storage) {
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
        var message = update.message();
        var split = message.text().split("\\s+", 2);
        var id = message.chat().id();
        if (split.length == 1) {
            return new SendMessage(id, NOT_FOUND_LINK);
        } else {
            try {
                URI uri = new URI(split[1]);
                var link = LinkUtil.parse(uri);
                var user = storage.findById(id);
                if (user.isPresent()) {
                    var links = user.get().getLinks();
                    var removed = links.remove(link);
                    if (removed) {
                        return new SendMessage(id, SUCCESS_UNTRACKING);
                    }
                }
                return new SendMessage(id, UNTRACKED_LINK);
            } catch (URISyntaxException e) {
                return new SendMessage(id, UNSUPPORTED_LINK);
            }
        }
    }
}
