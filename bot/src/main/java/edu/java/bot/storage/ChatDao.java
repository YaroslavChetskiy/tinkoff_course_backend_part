package edu.java.bot.storage;

import edu.java.bot.model.entity.UserChat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.springframework.stereotype.Component;

// Класс-заглушка, добавлен и используется до добавления БД
// При добавлении БД будет удалён/перенесён в scrapper
@Getter
@Component
public class ChatDao {

    private final List<UserChat> storage = new ArrayList<>();

    public void addChat(UserChat userChat) {
        storage.add(userChat);
    }

    public Optional<UserChat> findById(Long id) {
        return storage.stream()
            .filter(u -> u.getId().equals(id))
            .findFirst();
    }
}
