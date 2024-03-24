package edu.java.domain.repository.jpa;

import edu.java.dto.entity.hibernate.Chat;
import edu.java.scrapper.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext
class JpaChatRepositoryTest extends IntegrationTest {

    private static final Chat CHAT = Chat.builder()
        .id(255L)
        .createdAt(OffsetDateTime.now())
        .build();

    private static final Chat SECOND_CHAT = Chat.builder()
        .id(100L)
        .createdAt(OffsetDateTime.now())
        .build();

    @Autowired
    private JpaChatRepository chatRepository;

    @Test
    @Transactional
    @Rollback
    void saveAndFind() {
        var savedChat1 = chatRepository.save(CHAT);
        var savedChat2 = chatRepository.save(SECOND_CHAT);

        List<Chat> chats = chatRepository.findAll();

        assertThat(chats).hasSize(2);
        assertThat(chats).containsAll(List.of(savedChat1, savedChat2));
    }

    @Test
    @Transactional
    @Rollback
    void delete() {
        var savedChat1 = chatRepository.save(CHAT);
        var savedChat2 = chatRepository.save(SECOND_CHAT);

        chatRepository.delete(savedChat1);
        List<Chat> chats = chatRepository.findAll();

        assertThat(chats).hasSize(1);
        assertThat(chats).contains(savedChat2);

        chatRepository.deleteById(savedChat2.getId());

        chats = chatRepository.findAll();

        assertThat(chats).isEmpty();
    }

    @Test
    @Transactional
    @Rollback
    void findById() {
        var savedChat = chatRepository.save(CHAT);

        Optional<Chat> chat = chatRepository.findById(savedChat.getId());

        assertThat(chat).isPresent();
        assertThat(chat.get()).isEqualTo(savedChat);

        Optional<Chat> chat2 = chatRepository.findById(SECOND_CHAT.getId());
        assertThat(chat2).isEmpty();
    }

}
