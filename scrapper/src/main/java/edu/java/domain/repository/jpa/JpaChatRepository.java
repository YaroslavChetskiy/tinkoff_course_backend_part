package edu.java.domain.repository.jpa;

import edu.java.dto.entity.hibernate.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaChatRepository extends JpaRepository<Chat, Long> {
}
