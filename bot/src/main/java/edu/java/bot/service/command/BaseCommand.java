package edu.java.bot.service.command;

import edu.java.bot.service.messageProcessor.UserMessageProcessor;
import edu.java.bot.service.storage.UserStorage;

public abstract class BaseCommand implements Command {

    protected static final String UNSUPPORTED_LINK = """
        Неверный формат ссылки или ресурс.
        Пример ссылки: https://example.com/catalog/file?param1=value1&param2=value2..&param_n=value_n#anchor1
        Доступные для отслеживания ресурсы: Github, StackOverflow""";

    protected static final String NOT_FOUND_LINK = "Укажите ссылку.";

    protected final UserMessageProcessor processor;
    protected final UserStorage storage;

    public BaseCommand(UserMessageProcessor processor, UserStorage storage) {
        this.processor = processor;
        this.storage = storage;
    }

    @Override
    public String toString() {
        return command() + ": " + description();
    }
}
