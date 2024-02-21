package edu.java.dto.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record RepositoryResponse(
    @JsonProperty("full_name") String fullName,
    @JsonProperty("html_url") String url,
    @JsonProperty("updated_at") OffsetDateTime lastUpdateTime) {
}
