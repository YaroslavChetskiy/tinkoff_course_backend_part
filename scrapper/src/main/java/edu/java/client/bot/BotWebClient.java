package edu.java.client.bot;

import edu.java.dto.request.LinkUpdateRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class BotWebClient implements BotClient {

    private final WebClient webClient;

    public BotWebClient(@Value("${bot.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .build();
    }

    @Override
    public String sendUpdate(LinkUpdateRequest updateRequest) {
        return webClient.post()
            .uri("/updates")
            .body(BodyInserters.fromValue(updateRequest))
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }
}
