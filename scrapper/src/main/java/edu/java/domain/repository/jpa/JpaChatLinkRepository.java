package edu.java.domain.repository.jpa;

import edu.java.dto.entity.hibernate.ChatLink;
import edu.java.dto.entity.hibernate.link.Link;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaChatLinkRepository extends JpaRepository<ChatLink, Long> {

    boolean existsByChatIdAndLinkId(Long chatId, Long linkId);

    boolean existsByLinkId(Long linkId);

    Optional<ChatLink> findChatLinkByChatIdAndLinkUrl(Long chatId, String url);

    @Query("SELECT link FROM ChatLink WHERE chat.id = :chatId")
    List<Link> findAllLinksByChatId(Long chatId);

    @Query("SELECT chat.id FROM ChatLink WHERE link.id = :linkId")
    List<Long> findAllChatIdsByLinkId(Long linkId);
}
