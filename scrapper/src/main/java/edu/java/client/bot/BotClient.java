package edu.java.client.bot;

import edu.java.dto.request.LinkUpdateRequest;

public interface BotClient {

    String sendUpdate(LinkUpdateRequest updateRequest);
}
