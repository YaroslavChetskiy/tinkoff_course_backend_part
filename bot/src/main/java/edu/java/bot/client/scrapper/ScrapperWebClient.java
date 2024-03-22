package edu.java.bot.client.scrapper;

import edu.java.bot.model.dto.request.AddLinkRequest;
import edu.java.bot.model.dto.request.RemoveLinkRequest;
import edu.java.bot.model.dto.response.LinkResponse;
import edu.java.bot.model.dto.response.ListLinksResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ScrapperWebClient implements ScrapperClient {

    private static final String CHAT_ID_HEADER = "Tg-Chat-Id";
    private static final String CHAT_ENDPOINT = "/tg-chat/{id}";
    private static final String LINKS_ENDPOINT = "/links";

    private final WebClient webClient;

    public ScrapperWebClient(@Value("${scrapper.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

    @Override
    public String registerChat(Long chatId) {
        return webClient.post()
            .uri(CHAT_ENDPOINT, chatId)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
            .bodyToMono(String.class)
            .block();
    }

    @Override
    public String deleteChat(Long chatId) {
        return webClient.delete()
            .uri(CHAT_ENDPOINT, chatId)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
            .bodyToMono(String.class)
            .block();
    }

    @Override
    public ListLinksResponse getAllLinks(Long chatId) {
        return webClient.get()
            .uri(LINKS_ENDPOINT)
            .header(CHAT_ID_HEADER, String.valueOf(chatId))
            .retrieve()
            .bodyToMono(ListLinksResponse.class)
            .block();
    }

    @Override
    public LinkResponse addLink(Long chatId, AddLinkRequest addLinkRequest) {
        return webClient.post()
            .uri(LINKS_ENDPOINT)
            .header(CHAT_ID_HEADER, String.valueOf(chatId))
            .body(BodyInserters.fromValue(addLinkRequest))
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
            .bodyToMono(LinkResponse.class)
            .block();
    }

    @Override
    public LinkResponse removeLink(Long chatId, RemoveLinkRequest removeLinkRequest) {
        return webClient.method(HttpMethod.DELETE)
            .uri(LINKS_ENDPOINT)
            .header(CHAT_ID_HEADER, String.valueOf(chatId))
            .body(BodyInserters.fromValue(removeLinkRequest))
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.empty())
            .bodyToMono(LinkResponse.class)
            .block();
    }
}
