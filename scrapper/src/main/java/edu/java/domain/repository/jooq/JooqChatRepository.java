package edu.java.domain.repository.jooq;

import edu.java.domain.repository.ChatRepository;
import edu.java.dto.entity.Chat;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import static edu.java.domain.jooq.scrapper_schema.Tables.CHAT;

@Repository
@RequiredArgsConstructor
public class JooqChatRepository implements ChatRepository {

    private final DSLContext dslContext;

    @Override
    public Chat saveChat(Chat chat) {
        dslContext.insertInto(CHAT)
            .set(CHAT.ID, chat.getId())
            .execute();
        return findById(chat.getId());
    }

    @Override
    public Chat deleteChat(Long chatId) {
        var deleted = findById(chatId);
        dslContext.deleteFrom(CHAT)
            .where(CHAT.ID.eq(chatId))
            .execute();
        return deleted;
    }

    @Override
    public Chat findById(Long chatId) {
        return dslContext.select(CHAT.fields())
            .from(CHAT)
            .where(CHAT.ID.eq(chatId))
            .fetchOneInto(Chat.class);
    }
}
