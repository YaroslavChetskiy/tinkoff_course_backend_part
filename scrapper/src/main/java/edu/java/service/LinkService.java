package edu.java.service;

import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import java.util.Collections;
import org.springframework.stereotype.Service;

@Service
public class LinkService {

    private static final String DUMMY = "dummy";

    public ListLinksResponse getAllLinks(Long chatId) {
        return new ListLinksResponse(Collections.emptyList(), 0);
    }

    public LinkResponse addLink(Long chatId, AddLinkRequest request) {
        return new LinkResponse(0L, DUMMY);
    }

    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {
        return new LinkResponse(0L, DUMMY);
    }
}
