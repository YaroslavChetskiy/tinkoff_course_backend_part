package edu.java.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.bot.messageProcessor.UserMessageProcessor;
import edu.java.bot.model.dto.response.LinkResponse;
import edu.java.bot.model.dto.response.ListLinksResponse;

public class ListCommand extends BaseCommand {

    private static final String COMMAND = "/list";
    private static final String DESCRIPTION = "Показать список отслеживаемых ссылок.";

    private static final String EMPTY_LINK_LIST = "Вы не отслеживаете ни одной ссылки.";

    public ListCommand(UserMessageProcessor processor, ScrapperClient scrapperClient) {
        super(processor, scrapperClient);
    }

    @Override
    public SendMessage handle(Update update) {
        var id = update.message().chat().id();
        ListLinksResponse listLinksResponse = scrapperClient.getAllLinks(id);
        if (listLinksResponse.size() == 0) {
            return new SendMessage(id, EMPTY_LINK_LIST);
        }
        var links = listLinksResponse.links();
        StringBuilder stringBuilder = new StringBuilder("Список отслеживаемых ссылок:\n");
        for (LinkResponse link : links) {
            stringBuilder.append(" - ").append(link.url()).append("\n");
        }
        return new SendMessage(id, stringBuilder.toString()).disableWebPagePreview(true);
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
