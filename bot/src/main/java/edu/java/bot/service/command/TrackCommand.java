package edu.java.bot.service.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.model.entity.Link;
import edu.java.bot.model.entity.User;
import edu.java.bot.service.messageProcessor.UserMessageProcessor;
import edu.java.bot.service.storage.UserStorage;
import edu.java.bot.util.LinkUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class TrackCommand extends BaseCommand {

    private static final String COMMAND = "/track";
    private static final String DESCRIPTION = "Начать отслеживание ссылки.";

    private static final String SUCCESS_TRACKING = "Отслеживание ссылки начато успешно.";

    public TrackCommand(UserMessageProcessor processor, UserStorage storage) {
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
                if (!LinkUtil.supports(uri)) {
                    return new SendMessage(id, UNSUPPORTED_LINK);
                }
                Link link = LinkUtil.parse(uri);

                Optional<User> user = storage.findById(id);

                user.orElseGet(() -> {
                        var newUser = User.builder()
                            .id(id)
                            .build();
                        storage.addUser(newUser);
                        return newUser;
                    }
                ).getLinks().add(link);

                return new SendMessage(id, SUCCESS_TRACKING);
            } catch (URISyntaxException e) {
                return new SendMessage(id, UNSUPPORTED_LINK);
            }
        }
    }
}
