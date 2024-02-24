package edu.java.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record LinkUpdateRequest(Long id,
                                @NotBlank(message = "URL не должен быть пустым") String url,
                                String description,
                                @NotEmpty(message = "Список чатов не должен быть пустым") List<Long> tgChatIds) {

}
