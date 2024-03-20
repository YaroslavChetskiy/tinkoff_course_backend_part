package edu.java.domain.repository.jooq;

import edu.java.domain.repository.ChatLinkRepository;
import edu.java.dto.entity.jdbc.Link;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;
import static edu.java.domain.jooq.scrapper_schema.Tables.CHAT_LINK;
import static edu.java.domain.jooq.scrapper_schema.Tables.LINK;

@Repository
@RequiredArgsConstructor
public class JooqChatLinkRepository implements ChatLinkRepository {

    private final DSLContext dslContext;

    @Override
    public void addLinkToChat(Long chatId, Long linkId) {
        dslContext.insertInto(CHAT_LINK)
            .set(CHAT_LINK.CHAT_ID, chatId)
            .set(CHAT_LINK.LINK_ID, linkId)
            .execute();
    }

    @Override
    public void removeLinkFromChat(Long chatId, Long linkId) {
        dslContext.deleteFrom(CHAT_LINK)
            .where(CHAT_LINK.CHAT_ID.eq(chatId).and(CHAT_LINK.LINK_ID.eq(linkId)))
            .execute();
    }

    @Override
    public boolean isLinkTrackedInChat(Long chatId, Long linkId) {
        return Boolean.TRUE.equals(dslContext.select(
                DSL.exists(
                    DSL.selectOne()
                        .from(CHAT_LINK)
                        .where(CHAT_LINK.CHAT_ID.eq(chatId).and(CHAT_LINK.LINK_ID.eq(linkId)))
                )
            )
            .fetchOneInto(boolean.class));
    }

    @Override
    public boolean isLinkTracked(Long linkId) {
        return Boolean.TRUE.equals(dslContext.select(
                DSL.exists(
                    DSL.selectOne()
                        .from(CHAT_LINK)
                        .where(CHAT_LINK.LINK_ID.eq(linkId))
                )
            )
            .fetchOneInto(boolean.class));
    }

    @Override
    public List<Link> findAllLinksByChatId(Long chatId) {
        return dslContext.select(LINK.fields())
            .from(LINK)
            .join(CHAT_LINK).on(LINK.ID.eq(CHAT_LINK.LINK_ID))
            .where(CHAT_LINK.CHAT_ID.eq(chatId))
            .fetchInto(Link.class);
    }

    @Override
    public List<Long> findAllChatIdsByLinkId(Long linkId) {
        return dslContext.select(CHAT_LINK.CHAT_ID)
            .from(CHAT_LINK)
            .where(CHAT_LINK.LINK_ID.eq(linkId))
            .fetchInto(Long.class);
    }
}
