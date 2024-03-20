package edu.java.bot.messageProcessor;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.bot.command.Command;
import edu.java.bot.command.HelpCommand;
import edu.java.bot.command.ListCommand;
import edu.java.bot.command.StartCommand;
import edu.java.bot.command.TrackCommand;
import edu.java.bot.command.UntrackCommand;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

// простая реализация обработчика сообщений,
// возможно, в дальнейшем появится с цепочкой обязанностей или другим алгоритмом обработки
@Component
public class SimpleMessageProcessor implements UserMessageProcessor {

    private static final String UNKNOWN_COMMAND = "Неизвестная команда.";

    private final List<Command> commands = new ArrayList<>();

    public SimpleMessageProcessor(ScrapperClient scrapperClient) {
        commands.addAll(List.of(
            new StartCommand(this, scrapperClient),
            new HelpCommand(this, scrapperClient),
            new TrackCommand(this, scrapperClient),
            new UntrackCommand(this, scrapperClient),
            new ListCommand(this, scrapperClient)
        ));
    }

    @Override
    public SendMessage process(Update update) {
        Optional<Command> command = commands.stream()
            .filter(c -> c.supports(update))
            .findFirst();
        return command.map(value -> value.handle(update))
            .orElseGet(() -> new SendMessage(update.message().chat().id(), UNKNOWN_COMMAND));
    }

    @Override
    public List<? extends Command> commands() {
        return commands;
    }
}
