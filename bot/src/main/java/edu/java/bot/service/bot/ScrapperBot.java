package edu.java.bot.service.bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.response.BaseResponse;
import edu.java.bot.service.command.Command;
import edu.java.bot.service.messageProcessor.UserMessageProcessor;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ScrapperBot implements Bot {

    private static final int UPDATES_LIMIT = 100;
    private final TelegramBot bot;

    private final UserMessageProcessor processor;

    public ScrapperBot(TelegramBot telegramBot, UserMessageProcessor processor) {
        this.processor = processor;
        bot = telegramBot;
        bot.setUpdatesListener(this);
        execute(createMenu());
    }

    public SetMyCommands createMenu() {
        var commands = processor.commands().stream()
            .map(Command::toApiCommand)
            .toArray(BotCommand[]::new);
        return new SetMyCommands(commands);
    }

    @Override
    public <T extends BaseRequest<T, R>, R extends BaseResponse> void execute(BaseRequest<T, R> request) {
        bot.execute(request);
    }

    @Override
    public int process(List<Update> updates) {
        for (Update update : updates) {
            if (update.message() != null) {
                var sendMessage = processor.process(update);
                execute(sendMessage);
            }
        }
        return CONFIRMED_UPDATES_ALL;
    }

    @Override
    public void start() {
        GetUpdates getUpdates = new GetUpdates().limit(UPDATES_LIMIT).offset(0).timeout(0);
        var updatesResponse = bot.execute(getUpdates);
        if (updatesResponse.isOk()) {
            process(updatesResponse.updates());
        }
    }

    @Override
    public void close() {
        bot.removeGetUpdatesListener();
    }
}
