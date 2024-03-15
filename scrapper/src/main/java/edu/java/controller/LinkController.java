package edu.java.controller;

import edu.java.dto.request.AddLinkRequest;
import edu.java.dto.request.RemoveLinkRequest;
import edu.java.dto.response.LinkResponse;
import edu.java.dto.response.ListLinksResponse;
import edu.java.service.LinkService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/links")
public class LinkController {

    private static final String CHAT_ID_HEADER = "Tg-Chat-Id";

    private final LinkService linkService;

    public LinkController(@Qualifier("jdbcLinkService") LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping
    public ListLinksResponse getAllLinks(@RequestHeader(CHAT_ID_HEADER) Long chatId) {
        return linkService.getAllLinks(chatId);
    }

    @PostMapping
    public LinkResponse addLink(
        @RequestHeader(CHAT_ID_HEADER) Long chatId,
        @RequestBody @Valid AddLinkRequest addLinkRequest
    ) {
        return linkService.addLink(chatId, addLinkRequest);
    }

    @DeleteMapping
    public LinkResponse removeLink(
        @RequestHeader(CHAT_ID_HEADER) Long chatId,
        @RequestBody @Valid RemoveLinkRequest removeLinkRequest
    ) {
        return linkService.removeLink(chatId, removeLinkRequest);
    }
}
