package edu.java.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record EventResponse(
    @JsonProperty("type") EventType type,
    @JsonProperty("payload") Payload payload,
    @JsonProperty("created_at") OffsetDateTime createdAt
) {

    public record Payload(
        @JsonProperty("commits") List<Commit> commits,
        @JsonProperty("issue") Issue issue,

        @JsonProperty("pull_request") PullRequest pullRequest,

        @JsonProperty("comment") Comment comment
    ) {

        public record Commit(@JsonProperty("message") String message) {
        }

        public record Issue(@JsonProperty("number") int number, @JsonProperty("title") String title) {
        }

        public record Comment(@JsonProperty("body") String body) {
        }

        public record PullRequest(@JsonProperty("title") String title) {
        }
    }

}
