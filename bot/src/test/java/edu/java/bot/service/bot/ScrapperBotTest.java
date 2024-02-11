package edu.java.bot.service.bot;

import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.configuration.ApplicationConfig;
import edu.java.bot.service.command.Command;
import edu.java.bot.service.messageProcessor.SimpleMessageProcessor;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScrapperBotTest {
    private static final SimpleMessageProcessor PROCESSOR = mock(SimpleMessageProcessor.class);

    private static final ScrapperBot BOT = new ScrapperBot(
        new ApplicationConfig(""),
        PROCESSOR
    );

    private static final Chat CHAT = mock(Chat.class);
    private static final Message MESSAGE = mock(Message.class);
    private static final Update UPDATE = mock(Update.class);
    private static final long ID = 1L;

    private static final String TEST_MESSAGE = "/start";

    @BeforeAll
    static void prepare() {
        when(CHAT.id()).thenReturn(ID);
        when(MESSAGE.chat()).thenReturn(CHAT);
        when(MESSAGE.text()).thenReturn(TEST_MESSAGE);
        when(UPDATE.message()).thenReturn(MESSAGE);
    }

    @Test
    @DisplayName("Создание меню кнопок")
    void createMenu() {
        var botCommand1 = new BotCommand("c1", "d1");
        var botCommand2 = new BotCommand("c2", "d2");

        Command mockCommand1 = mock(Command.class);
        when(mockCommand1.toApiCommand()).thenReturn(botCommand1);

        Command mockCommand2 = mock(Command.class);
        when(mockCommand2.toApiCommand()).thenReturn(botCommand2);

        List<? extends Command> commands = List.of(mockCommand1, mockCommand2);

        Mockito.<List<? extends Command>>when(PROCESSOR.commands()).thenReturn(commands);

        var actualResult = BOT.createMenu();

        var parameters = actualResult.getParameters().get("commands");

        assertThat(parameters).isEqualTo(new BotCommand[] {botCommand1, botCommand2});
    }

}
