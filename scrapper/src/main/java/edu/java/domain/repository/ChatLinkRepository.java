package edu.java.domain.repository;

import edu.java.dto.entity.Link;
import java.util.List;

public interface ChatLinkRepository {

    void addLinkToChat(Long chatId, Long linkId);

    void removeLinkFromChat(Long chatId, Long linkId);

    boolean isLinkTrackedInChat(Long chatId, Long linkId);

    boolean isLinkTracked(Long linkId);

    List<Link> findAllLinksByChatId(Long chatId);

    List<Long> findAllChatIdsByLinkId(Long linkId);
}
