package edu.java.bot.client.scrapper;

import edu.java.bot.model.dto.request.AddLinkRequest;
import edu.java.bot.model.dto.request.RemoveLinkRequest;
import edu.java.bot.model.dto.response.LinkResponse;
import edu.java.bot.model.dto.response.ListLinksResponse;

public interface ScrapperClient {

    String registerChat(Long chatId);

    String deleteChat(Long chatId);

    ListLinksResponse getAllLinks(Long chatId);

    LinkResponse addLink(Long chatId, AddLinkRequest addLinkRequest);

    LinkResponse removeLink(Long chatId, RemoveLinkRequest removeLinkRequest);
}
