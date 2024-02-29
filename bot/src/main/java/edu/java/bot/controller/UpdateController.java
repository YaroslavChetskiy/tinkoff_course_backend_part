package edu.java.bot.controller;

import edu.java.bot.model.dto.request.LinkUpdateRequest;
import edu.java.bot.service.BotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updates")
public class UpdateController {

    private final BotService botService;

    public UpdateController(BotService botService) {
        this.botService = botService;
    }

    @PostMapping
    public ResponseEntity<String> sendUpdate(@RequestBody @Valid LinkUpdateRequest updateRequest) {
        botService.sendUpdate(updateRequest);
        return ResponseEntity.ok("Обновление обработано");
    }
}
