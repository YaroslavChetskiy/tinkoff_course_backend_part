package edu.java.bot.messageProcessor;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.client.scrapper.ScrapperClient;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimpleMessageProcessorTest {

    private static final String UNKNOWN_COMMAND = "Неизвестная команда.";

    private static final Chat CHAT = mock(Chat.class);
    private static final Message MESSAGE = mock(Message.class);
    private static final Update UPDATE = mock(Update.class);

    private static final Long ID = 1L;

    @Mock
    private ScrapperClient scrapperClient;

    @InjectMocks
    private SimpleMessageProcessor processor;

    @BeforeAll
    static void prepare() {
        when(CHAT.id()).thenReturn(ID);
        when(MESSAGE.chat()).thenReturn(CHAT);
        when(UPDATE.message()).thenReturn(MESSAGE);
    }

    @ParameterizedTest
    @DisplayName("Получение сообщения о неизвестной команде от пользователя")
    @MethodSource("getArgumentsForGetUnknownCommandSendMessageAfterProcessingUpdateTest")
    void getUnknownCommandSendMessageAfterProcessingUpdate(String command, String expectedMessage) {
        when(MESSAGE.text()).thenReturn(command);
        var actualResult = processor.process(UPDATE);

        var parameters = actualResult.getParameters();

        assertThat(parameters.get("chat_id")).isEqualTo(ID);
        assertThat(parameters.get("text")).isEqualTo(expectedMessage);
    }

    static Stream<Arguments> getArgumentsForGetUnknownCommandSendMessageAfterProcessingUpdateTest() {
        return Stream.of(
            Arguments.of("/begin", UNKNOWN_COMMAND),
            Arguments.of("hello", UNKNOWN_COMMAND),
            Arguments.of("1234/", UNKNOWN_COMMAND)
        );
    }
}
