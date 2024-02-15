package edu.java.bot.service.storage;

import edu.java.bot.model.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.springframework.stereotype.Component;

// Класс-заглушка, добавлен и используется до добавления БД, вынесется в UserDao
@Getter
@Component
public class UserStorage {

    private final List<User> storage = new ArrayList<>();

    public void addUser(User user) {
        storage.add(user);
    }

    public Optional<User> findById(Long id) {
        return storage.stream()
            .filter(u -> u.getId().equals(id))
            .findFirst();
    }
}
