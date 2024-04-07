package edu.java.bot.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.client.scrapper.ScrapperClient;
import edu.java.bot.messageProcessor.SimpleMessageProcessor;
import edu.java.bot.model.dto.request.AddLinkRequest;
import edu.java.bot.model.dto.request.RemoveLinkRequest;
import edu.java.bot.model.dto.response.LinkResponse;
import edu.java.bot.model.dto.response.ListLinksResponse;
import edu.java.bot.model.entity.UserChat;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandTest {

    private static final String LIST_OF_COMMANDS = """
        Список команд:
        - /start: Начать работу.
        - /help: Показать список доступных команд.
        - /track: Начать отслеживание ссылки.
        - /untrack: Прекратить отслеживание ссылки.
        - /list: Показать список отслеживаемых ссылок.
        """;

    private static final String LIST_OF_LINKS = """
        Список отслеживаемых ссылок:
         - https://github.com/sanyarnd/tinkoff-java-course-2023/
         - https://stackoverflow.com/questions/123
        """;

    private static final String START_MESSAGE = "Бот запущен! Он поможет вам с отслеживанием ссылок.";

    private static final String REPEATED_REGISTRATION_MESSAGE = "Бот уже запущен.";

    private static final String SUCCESS_TRACKING = "Отслеживание ссылки начато успешно.";

    private static final String ALREADY_TACKED = "Вы уже отслеживаете данную ссылку.";

    protected static final String UNSUPPORTED_LINK = """
        Неверный формат ссылки или ресурс.
        Доступные для отслеживания ресурсы: Github, StackOverflow

        Формат ссылок:
        Github: https://www.github.com/{owner}/{repository}
        StackOverflow: https://www.stackoverflow.com/questions/{question_id}/{question_title}*

        * - опционально""";

    protected static final String NOT_FOUND_LINK = "Укажите ссылку.";

    private static final String UNTRACKED_LINK = "Вы и так не отслеживаете данную ссылку.";

    private static final String SUCCESS_UNTRACKING = "Отслеживание ссылки прекращено.";

    private static final String TEST_LINK = "https://stackoverflow.com/questions/123";

    private static final String EMPTY_LINK_LIST = "Вы не отслеживаете ни одной ссылки.";

    private static final UserChat USER_USER_CHAT = UserChat.builder()
        .id(1L)
        .build();

    private static final Chat CHAT = mock(com.pengrad.telegrambot.model.Chat.class);
    private static final Message MESSAGE = mock(Message.class);
    private static final Update UPDATE = mock(Update.class);

    private static ScrapperClient scrapperClient;

    private static SimpleMessageProcessor processor;

    @BeforeAll
    static void prepare() {
        scrapperClient = mock(ScrapperClient.class);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        processor = new SimpleMessageProcessor(scrapperClient, meterRegistry);

        when(CHAT.id()).thenReturn(USER_USER_CHAT.getId());
        when(MESSAGE.chat()).thenReturn(CHAT);
        when(UPDATE.message()).thenReturn(MESSAGE);
    }

    @ParameterizedTest
    @DisplayName("Получение способности поддержки команды")
    @MethodSource("getArgumentsForSupportCapabilityTest")
    void getCorrectSupportCapability(Command command, String text, boolean expectedResult) {
        when(MESSAGE.text()).thenReturn(text);

        var actualResult = command.supports(UPDATE);
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    static Stream<Arguments> getArgumentsForSupportCapabilityTest() {
        return Stream.of(
            Arguments.of(new HelpCommand(processor, scrapperClient), "/help", true),
            Arguments.of(new HelpCommand(processor, scrapperClient), "dummy", false),
            Arguments.of(new ListCommand(processor, scrapperClient), "/list", true),
            Arguments.of(new ListCommand(processor, scrapperClient), "dummy", false),
            Arguments.of(new StartCommand(processor, scrapperClient), "/start", true),
            Arguments.of(new StartCommand(processor, scrapperClient), "dummy", false),
            Arguments.of(new TrackCommand(processor, scrapperClient), "/track", true),
            Arguments.of(new TrackCommand(processor, scrapperClient), "/track github.com", true),
            Arguments.of(new TrackCommand(processor, scrapperClient), "dummy", false),
            Arguments.of(new UntrackCommand(processor, scrapperClient), "/untrack", true),
            Arguments.of(new UntrackCommand(processor, scrapperClient), "/untrack github.com", true),
            Arguments.of(new UntrackCommand(processor, scrapperClient), "dummy", false)
        );
    }

    @ParameterizedTest
    @DisplayName("Получение списка отслеживаемых ресурсов")
    @MethodSource("getArgumentsForGetListOfLinksInListCommandHandle")
    void getListOfLinksInListCommandHandle(ListLinksResponse response, String expectedText) {
        Command command = new ListCommand(processor, scrapperClient);

        when(scrapperClient.getAllLinks(anyLong())).thenReturn(response);

        checkParamsInSendMessage(command, expectedText);
    }

    static Stream<Arguments> getArgumentsForGetListOfLinksInListCommandHandle() {
        return Stream.of(
            Arguments.of(
                new ListLinksResponse(
                    List.of(
                        new LinkResponse(1L, "https://github.com/sanyarnd/tinkoff-java-course-2023/"),
                        new LinkResponse(2L, TEST_LINK)
                    ),
                    2
                ),
                LIST_OF_LINKS
            ),
            Arguments.of(new ListLinksResponse(null, 0), EMPTY_LINK_LIST)
        );
    }

    @Test
    @DisplayName("Получение правильной справки о командах")
    void getCorrectSendMessageInHelpCommandHandle() {
        var helpCommand = new HelpCommand(processor, scrapperClient);
        checkParamsInSendMessage(helpCommand, LIST_OF_COMMANDS);
    }

    @ParameterizedTest
    @DisplayName("Обработка команды /start")
    @MethodSource("getArgumentsForGetCorrectMessageInStartCommandHandle")
    void getCorrectMessageInStartCommandHandle(String response, String expectedText) {
        var startCommand = new StartCommand(processor, scrapperClient);

        when(scrapperClient.registerChat(anyLong())).thenReturn(response);

        checkParamsInSendMessage(startCommand, expectedText);
    }

    static Stream<Arguments> getArgumentsForGetCorrectMessageInStartCommandHandle() {
        return Stream.of(
            Arguments.of("Чат успешно зарегистрирован", START_MESSAGE),
            Arguments.of(null, REPEATED_REGISTRATION_MESSAGE)
        );
    }

    @ParameterizedTest()
    @MethodSource("getArgumentsForGetCorrectMessageInTrackCommandHandleTest")
    void getCorrectMessageInTrackCommandHandleIfCommandIsIncorrect(String command, String expectedText) {
        when(MESSAGE.text()).thenReturn(command);

        var trackCommand = new TrackCommand(processor, scrapperClient);

        checkParamsInSendMessage(trackCommand, expectedText);
    }

    static Stream<Arguments> getArgumentsForGetCorrectMessageInTrackCommandHandleTest() {
        return Stream.of(
            Arguments.of("/track", NOT_FOUND_LINK),
            Arguments.of("/track dummy.com", UNSUPPORTED_LINK),
            Arguments.of("/track what123", UNSUPPORTED_LINK)
        );
    }

    @ParameterizedTest
    @DisplayName("Получение корректного сообщения при верной команде от пользователя")
    @MethodSource("getArgumentsForGetCorrectMessageInTrackCommandHandleIfCommandIsCorrect")
    void getCorrectMessageInTrackCommandHandleIfCommandIsCorrect(
        String command,
        LinkResponse linkResponse,
        String expectedText
    ) {
        var trackCommand = new TrackCommand(processor, scrapperClient);

        when(MESSAGE.text()).thenReturn(command);
        when(scrapperClient.addLink(anyLong(), any(AddLinkRequest.class))).thenReturn(linkResponse);

        checkParamsInSendMessage(trackCommand, expectedText);
    }

    static Stream<Arguments> getArgumentsForGetCorrectMessageInTrackCommandHandleIfCommandIsCorrect() {
        return Stream.of(
            Arguments.of("/track " + TEST_LINK, new LinkResponse(1L, TEST_LINK), SUCCESS_TRACKING),
            Arguments.of("/track " + TEST_LINK, new LinkResponse(null, null), ALREADY_TACKED)
        );
    }

    @ParameterizedTest()
    @MethodSource("getArgumentsForGetCorrectMessageInUntrackCommandHandleIfCommandIsValidTest")
    void getCorrectMessageInUntrackCommandHandleIfCommandIsValid(
        String command,
        LinkResponse linkResponse,
        String expectedText
    ) {
        var untrackCommand = new UntrackCommand(processor, scrapperClient);

        when(scrapperClient.removeLink(anyLong(), any(RemoveLinkRequest.class))).thenReturn(linkResponse);

        when(MESSAGE.text()).thenReturn(command);

        checkParamsInSendMessage(untrackCommand, expectedText);
    }

    static Stream<Arguments> getArgumentsForGetCorrectMessageInUntrackCommandHandleIfCommandIsValidTest() {
        return Stream.of(
            Arguments.of("/untrack dummy.com", new LinkResponse(null, null), UNTRACKED_LINK),
            Arguments.of("/untrack " + TEST_LINK, new LinkResponse(1L, TEST_LINK), SUCCESS_UNTRACKING),
            Arguments.of("/untrack " + TEST_LINK, new LinkResponse(null, null), UNTRACKED_LINK)
        );
    }

    @ParameterizedTest()
    @MethodSource("getArgumentsForGetCorrectMessageInUntrackCommandHandleIfCommandIsInvalidTest")
    void getCorrectMessageInUntrackCommandHandleIfCommandIsInvalid(String command, String expectedText) {
        var untrackCommand = new UntrackCommand(processor, scrapperClient);

        when(MESSAGE.text()).thenReturn(command);

        checkParamsInSendMessage(untrackCommand, expectedText);
    }

    static Stream<Arguments> getArgumentsForGetCorrectMessageInUntrackCommandHandleIfCommandIsInvalidTest() {
        return Stream.of(
            Arguments.of("/untrack", NOT_FOUND_LINK),
            Arguments.of("/untrack `", UNSUPPORTED_LINK)
        );
    }

    private static void checkParamsInSendMessage(Command command, String text) {
        var actualResult = command.handle(UPDATE);
        var parameters = actualResult.getParameters();

        assertThat(parameters.get("chat_id")).isEqualTo(USER_USER_CHAT.getId());
        assertThat(parameters.get("text")).isEqualTo(text);
    }
}
