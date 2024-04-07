package edu.java.bot.messageProcessor;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.client.scrapper.ScrapperClient;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

    private SimpleMessageProcessor processor;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @BeforeAll
    static void prepare() {
        when(CHAT.id()).thenReturn(ID);
        when(MESSAGE.chat()).thenReturn(CHAT);
        when(UPDATE.message()).thenReturn(MESSAGE);
    }

    @BeforeEach
    void setUp() {
        ScrapperClient scrapperClient = mock(ScrapperClient.class);
        processor = new SimpleMessageProcessor(scrapperClient, meterRegistry);
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

        double messagesProcessedTotal = meterRegistry.counter("messages_processed_total").count();
        assertThat(messagesProcessedTotal).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Проверка увеличения метрик")
    void checkIncrementMetricsCounter() {
        var messages = List.of("/begin", "hello", "/start", "/help", "/start");
        for (String message : messages) {
            when(MESSAGE.text()).thenReturn(message);
            processor.process(UPDATE);
        }

        double messagesProcessedTotal = meterRegistry.counter("messages_processed_total").count();
        assertThat(messagesProcessedTotal).isEqualTo(messages.size());

        double startMessagesProcessedCount = meterRegistry.counter(
            "command_processed_total",
            "command_type",
            "/start"
        ).count();

        assertThat(startMessagesProcessedCount).isEqualTo(2.0);

        double helpMessagesProcessedCount = meterRegistry.counter(
            "command_processed_total",
            "command_type",
            "/help"
        ).count();

        assertThat(helpMessagesProcessedCount).isEqualTo(1.0);
    }

    static Stream<Arguments> getArgumentsForGetUnknownCommandSendMessageAfterProcessingUpdateTest() {
        return Stream.of(
            Arguments.of("/begin", UNKNOWN_COMMAND),
            Arguments.of("hello", UNKNOWN_COMMAND),
            Arguments.of("1234/", UNKNOWN_COMMAND)
        );
    }
}
