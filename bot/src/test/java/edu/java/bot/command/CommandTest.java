package edu.java.bot.command;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import edu.java.bot.messageProcessor.SimpleMessageProcessor;
import edu.java.bot.model.entity.Link;
import edu.java.bot.model.entity.UserChat;
import edu.java.bot.storage.ChatDao;
import edu.java.bot.util.LinkUtil;
import java.net.URI;
import java.net.URISyntaxException;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommandTest {

    private static final ChatDao STORAGE = new ChatDao();

    private static final SimpleMessageProcessor PROCESSOR = new SimpleMessageProcessor(STORAGE);

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
         - https://stackoverflow.com/
        """;

    private static final String START_MESSAGE = "Бот запущен! Он поможет вам с отслеживанием ссылок.";

    private static final String SUCCESS_TRACKING = "Отслеживание ссылки начато успешно.";

    protected static final String UNSUPPORTED_LINK = """
        Неверный формат ссылки или ресурс.
        Пример ссылки: https://example.com/catalog/file?param1=value1&param2=value2..&param_n=value_n#anchor1
        Доступные для отслеживания ресурсы: Github, StackOverflow""";

    protected static final String NOT_FOUND_LINK = "Укажите ссылку.";

    private static final String UNTRACKED_LINK = "Вы не отслеживаете данную ссылку.";

    private static final String SUCCESS_UNTRACKING = "Отслеживание ссылки прекращено.";

    private static final String TEST_LINK = "https://stackoverflow.com/search";

    private static final String EMPTY_LINK_LIST = "Вы не отслеживаете ни одной ссылки.";

    private static final UserChat USER_USER_CHAT = UserChat.builder()
        .id(1L)
        .build();

    private static final Chat CHAT = mock(com.pengrad.telegrambot.model.Chat.class);
    private static final Message MESSAGE = mock(Message.class);
    private static final Update UPDATE = mock(Update.class);

    @BeforeAll
    static void prepare() {
        STORAGE.addChat(USER_USER_CHAT);
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
            Arguments.of(new HelpCommand(PROCESSOR, STORAGE), "/help", true),
            Arguments.of(new HelpCommand(PROCESSOR, STORAGE), "dummy", false),
            Arguments.of(new ListCommand(PROCESSOR, STORAGE), "/list", true),
            Arguments.of(new ListCommand(PROCESSOR, STORAGE), "dummy", false),
            Arguments.of(new StartCommand(PROCESSOR, STORAGE), "/start", true),
            Arguments.of(new StartCommand(PROCESSOR, STORAGE), "dummy", false),
            Arguments.of(new TrackCommand(PROCESSOR, STORAGE), "/track", true),
            Arguments.of(new TrackCommand(PROCESSOR, STORAGE), "/track github.com", true),
            Arguments.of(new TrackCommand(PROCESSOR, STORAGE), "dummy", false),
            Arguments.of(new UntrackCommand(PROCESSOR, STORAGE), "/untrack", true),
            Arguments.of(new UntrackCommand(PROCESSOR, STORAGE), "/untrack github.com", true),
            Arguments.of(new UntrackCommand(PROCESSOR, STORAGE), "dummy", false)
        );
    }

    @Test
    @DisplayName("Получение списка отслеживаемых ресурсов")
    void getListOfLinksInListCommandHandle() throws URISyntaxException {

        Command command = new ListCommand(PROCESSOR, STORAGE);
        checkParamsInSendMessage(command, EMPTY_LINK_LIST);

        Link link1 = LinkUtil.parse(new URI("https://github.com/sanyarnd/tinkoff-java-course-2023/"));
        Link link2 = LinkUtil.parse(new URI("https://stackoverflow.com/"));
        USER_USER_CHAT.getLinks().add(link1);
        USER_USER_CHAT.getLinks().add(link2);

        checkParamsInSendMessage(command, LIST_OF_LINKS);

        USER_USER_CHAT.getLinks().remove(link1);
        USER_USER_CHAT.getLinks().remove(link2);
    }

    @ParameterizedTest
    @DisplayName("Получение нужного сообщения при обработке команды (в которых не используются ссылки)")
    @MethodSource("getArgumentsForGetCorrectSendMessageTest")
    void getCorrectSendMessageInCommandHandleThatNoRelatedToLinks(Command command, String text) {
        checkParamsInSendMessage(command, text);
    }

    static Stream<Arguments> getArgumentsForGetCorrectSendMessageTest() {
        return Stream.of(
            Arguments.of(new HelpCommand(PROCESSOR, STORAGE), LIST_OF_COMMANDS),
            Arguments.of(new StartCommand(PROCESSOR, STORAGE), START_MESSAGE)
        );
    }

    @ParameterizedTest()
    @MethodSource("getArgumentsForGetCorrectMessageInTrackCommandHandleTest")
    void getCorrectMessageInTrackCommandHandle(String command, String expectedText) throws URISyntaxException {
        when(MESSAGE.text()).thenReturn(command);
        checkParamsInSendMessage(new TrackCommand(PROCESSOR, STORAGE), expectedText);
        var split = command.split("\\s+", 2);
        if (expectedText.equals(SUCCESS_TRACKING)) {
            var link = LinkUtil.parse(new URI(split[1]));
            assertThat(USER_USER_CHAT.getLinks()).contains(link);
            USER_USER_CHAT.getLinks().clear();
        }
    }

    static Stream<Arguments> getArgumentsForGetCorrectMessageInTrackCommandHandleTest() {
        return Stream.of(
            Arguments.of("/track", NOT_FOUND_LINK),
            Arguments.of("/track dummy.com", UNSUPPORTED_LINK),
            Arguments.of("/track what123", UNSUPPORTED_LINK),
            Arguments.of("/track " + TEST_LINK, SUCCESS_TRACKING)
        );
    }

    @Test
    void addingNewUserIfNotExistInTrackHandle() throws URISyntaxException {
        var id = 2L;
        UserChat userChat = UserChat.builder()
            .id(id)
            .build();
        String command = "/track " + TEST_LINK;
        userChat.getLinks().add(LinkUtil.parse(new URI(TEST_LINK)));
        when(CHAT.id()).thenReturn(id);
        when(MESSAGE.text()).thenReturn(command);

        var trackCommand = new TrackCommand(PROCESSOR, STORAGE);
        trackCommand.handle(UPDATE);

        assertThat(STORAGE.getStorage()).contains(userChat);
    }

    @ParameterizedTest()
    @MethodSource("getArgumentsForGetCorrectMessageInUntrackCommandHandleTest")
    void getCorrectMessageInUntrackCommandHandle(String command, String expectedText) throws URISyntaxException {
        var link = LinkUtil.parse(new URI(TEST_LINK));
        USER_USER_CHAT.getLinks().add(link);
        when(MESSAGE.text()).thenReturn(command);
        checkParamsInSendMessage(new UntrackCommand(PROCESSOR, STORAGE), expectedText);
        if (expectedText.equals(SUCCESS_UNTRACKING)) {
            assertThat(USER_USER_CHAT.getLinks()).doesNotContain(link);
        }
        USER_USER_CHAT.getLinks().clear();
    }

    static Stream<Arguments> getArgumentsForGetCorrectMessageInUntrackCommandHandleTest() {
        return Stream.of(
            Arguments.of("/untrack", NOT_FOUND_LINK),
            Arguments.of("/untrack dummy.com", UNTRACKED_LINK),
            Arguments.of("/untrack what123", UNTRACKED_LINK),
            Arguments.of("/untrack " + TEST_LINK, SUCCESS_UNTRACKING)
        );
    }

    private static void checkParamsInSendMessage(Command command, String text) {
        var actualResult = command.handle(UPDATE);
        var parameters = actualResult.getParameters();

        assertThat(parameters.get("chat_id")).isEqualTo(USER_USER_CHAT.getId());
        assertThat(parameters.get("text")).isEqualTo(text);
    }
}
