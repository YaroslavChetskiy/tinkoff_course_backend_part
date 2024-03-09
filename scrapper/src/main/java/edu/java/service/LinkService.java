package edu.java.service;

import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;

public interface LinkService {

    ListLinksResponse getAllLinks(Long chatId);

    LinkResponse addLink(Long chatId, AddLinkRequest request);

    LinkResponse removeLink(Long chatId, RemoveLinkRequest request);

}
