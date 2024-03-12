package edu.java.dto.github;

import com.fasterxml.jackson.annotation.JsonCreator;
import edu.java.dto.github.EventResponse.Payload;
import java.util.HashMap;
import java.util.Map;

public enum EventType {
    PUSH {
        @Override
        public String generateUpdateMessage(Payload payload) {
            StringBuilder stringBuilder = new StringBuilder("Обновление: произошёл push в репозиторий\n");
            stringBuilder.append("Подробности:\nCommits:\n");
            for (int i = 0; i < payload.commits().size(); i++) {
                stringBuilder.append("\tCommit %d: %s\n".formatted(i + 1, payload.commits().get(i).message()));
            }
            return stringBuilder.toString();
        }
    },
    ISSUE_COMMENT {
        @Override
        public String generateUpdateMessage(Payload payload) {
            return "Обновление: Issue[%d][%s]: Добавлен новый комментарий:\n%s\n"
                .formatted(payload.issue().number(), payload.issue().title(), payload.comment().body());
        }
    },
    PULL_REQUEST {
        @Override
        public String generateUpdateMessage(Payload payload) {
            return "Обновление: создан новый pull request[%s]".formatted(payload.pullRequest().title());
        }
    },

    UNKNOWN {
        @Override
        public String generateUpdateMessage(Payload payload) {
            return "В репозитории произошло обновление";
        }
    };

    private static final Map<String, EventType> NAMES_MAP = new HashMap<>();

    static {
        NAMES_MAP.put("PushEvent", PUSH);
        NAMES_MAP.put("IssueCommentEvent", ISSUE_COMMENT);
        NAMES_MAP.put("PullRequestEvent", PULL_REQUEST);
    }

    public abstract String generateUpdateMessage(Payload payload);

    @JsonCreator
    public static EventType forValue(String value) {
        return NAMES_MAP.getOrDefault(value, UNKNOWN);
    }
}
