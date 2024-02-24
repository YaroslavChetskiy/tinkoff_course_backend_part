package edu.java.bot.model.dto.request;

import jakarta.validation.constraints.NotBlank;

// насколько целесообразно создать общий модуль common и положить туда *Request/*Response классы?
public record AddLinkRequest(@NotBlank(message = "Ссылка не должна быть пуста") String link) {
}
