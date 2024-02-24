package edu.java.bot.messageProcessor;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.storage.ChatDao;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimpleMessageProcessorTest {

    private static final ChatDao STORAGE = new ChatDao();

    private static final String START_MESSAGE = "Бот запущен! Он поможет вам с отслеживанием ссылок.";

    private static final String EMPTY_LINK_LIST = "Вы не отслеживаете ни одной ссылки.";

    private static final String UNKNOWN_COMMAND = "Неизвестная команда.";

    private static final Chat CHAT = mock(Chat.class);
    private static final Message MESSAGE = mock(Message.class);
    private static final Update UPDATE = mock(Update.class);

    private static final Long ID = 1L;

    @BeforeAll
    static void prepare() {
        when(CHAT.id()).thenReturn(ID);
        when(MESSAGE.chat()).thenReturn(CHAT);
        when(UPDATE.message()).thenReturn(MESSAGE);
    }

    @ParameterizedTest
    @DisplayName("Получение нужного сообщения для пользователя при разных командах от пользователя")
    @MethodSource("getArgumentsForGetCorrectSendMessageAfterProcessingUpdateTest")
    void getCorrectSendMessageAfterProcessingUpdate(String command, String expectedMessage) {
        var processor = new SimpleMessageProcessor(STORAGE);
        when(MESSAGE.text()).thenReturn(command);
        var actualResult = processor.process(UPDATE);

        var parameters = actualResult.getParameters();

        assertThat(parameters.get("chat_id")).isEqualTo(ID);
        assertThat(parameters.get("text")).isEqualTo(expectedMessage);
    }

    static Stream<Arguments> getArgumentsForGetCorrectSendMessageAfterProcessingUpdateTest() {
        return Stream.of(
            Arguments.of("/start", START_MESSAGE),
            Arguments.of("/list", EMPTY_LINK_LIST),
            Arguments.of("/begin", UNKNOWN_COMMAND),
            Arguments.of("hello", UNKNOWN_COMMAND),
            Arguments.of("1234/", UNKNOWN_COMMAND)
        );
    }
}
