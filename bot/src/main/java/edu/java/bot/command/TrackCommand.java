package edu.java.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.bot.messageProcessor.UserMessageProcessor;
import edu.java.bot.model.dto.request.AddLinkRequest;
import edu.java.bot.util.LinkUtil;
import java.net.URI;
import java.net.URISyntaxException;

public class TrackCommand extends BaseCommand {

    private static final String COMMAND = "/track";
    private static final String DESCRIPTION = "Начать отслеживание ссылки.";

    private static final String SUCCESS_TRACKING = "Отслеживание ссылки начато успешно.";
    private static final String ALREADY_TACKED = "Вы уже отслеживаете данную ссылку.";

    public TrackCommand(UserMessageProcessor processor, ScrapperClient scrapperClient) {
        super(processor, scrapperClient);
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
        var chatId = message.chat().id();
        if (split.length == 1) {
            return new SendMessage(chatId, NOT_FOUND_LINK);
        } else {
            try {
                URI uri = new URI(split[1]);
                if (!LinkUtil.supports(uri)) {
                    return new SendMessage(chatId, UNSUPPORTED_LINK);
                }
                var linkResponse = scrapperClient.addLink(chatId, new AddLinkRequest(uri.toString()));

                return new SendMessage(chatId, linkResponse.id() == null ? ALREADY_TACKED : SUCCESS_TRACKING);
            } catch (URISyntaxException e) {
                return new SendMessage(chatId, UNSUPPORTED_LINK);
            }
        }
    }
}
