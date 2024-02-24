package edu.java.bot.command;

import edu.java.bot.messageProcessor.UserMessageProcessor;
import edu.java.bot.storage.ChatDao;

public abstract class BaseCommand implements Command {

    protected static final String UNSUPPORTED_LINK = """
        Неверный формат ссылки или ресурс.
        Пример ссылки: https://example.com/catalog/file?param1=value1&param2=value2..&param_n=value_n#anchor1
        Доступные для отслеживания ресурсы: Github, StackOverflow""";

    protected static final String NOT_FOUND_LINK = "Укажите ссылку.";

    protected final UserMessageProcessor processor;
    protected final ChatDao storage;

    public BaseCommand(UserMessageProcessor processor, ChatDao storage) {
        this.processor = processor;
        this.storage = storage;
    }

    @Override
    public String toString() {
        return command() + ": " + description();
    }
}
