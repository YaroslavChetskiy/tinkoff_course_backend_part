package edu.java.bot.service.messageProcessor;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import edu.java.bot.service.command.Command;
import edu.java.bot.service.command.HelpCommand;
import edu.java.bot.service.command.ListCommand;
import edu.java.bot.service.command.StartCommand;
import edu.java.bot.service.command.TrackCommand;
import edu.java.bot.service.command.UntrackCommand;
import edu.java.bot.service.storage.UserStorage;
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

    public SimpleMessageProcessor(UserStorage storage) {
        commands.addAll(List.of(
            new StartCommand(this, storage),
            new HelpCommand(this, storage),
            new TrackCommand(this, storage),
            new UntrackCommand(this, storage),
            new ListCommand(this, storage)
        ));
    }

    @Override
    public List<? extends Command> commands() {
        return commands;
    }

    @Override
    public SendMessage process(Update update) {
        Optional<Command> command = commands.stream()
            .filter(c -> c.supports(update))
            .findFirst();
        return command.map(value -> value.handle(update))
            .orElse(new SendMessage(update.message().chat().id(), UNKNOWN_COMMAND));
    }
}