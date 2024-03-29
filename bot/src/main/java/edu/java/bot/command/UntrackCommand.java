package edu.java.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.bot.messageProcessor.UserMessageProcessor;
import edu.java.bot.model.dto.request.RemoveLinkRequest;
import java.net.URI;
import java.net.URISyntaxException;

public class UntrackCommand extends BaseCommand {

    private static final String COMMAND = "/untrack";
    private static final String DESCRIPTION = "Прекратить отслеживание ссылки.";

    private static final String UNTRACKED_LINK = "Вы и так не отслеживаете данную ссылку.";

    private static final String SUCCESS_UNTRACKING = "Отслеживание ссылки прекращено.";

    public UntrackCommand(UserMessageProcessor processor, ScrapperClient scrapperClient) {
        super(processor, scrapperClient);
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

                var linkResponse = scrapperClient.removeLink(chatId, new RemoveLinkRequest(uri.toString()));

                return new SendMessage(chatId, linkResponse.id() == null ? UNTRACKED_LINK : SUCCESS_UNTRACKING);
            } catch (URISyntaxException e) {
                return new SendMessage(chatId, UNSUPPORTED_LINK);
            }
        }
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
