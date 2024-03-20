package edu.java.client.github;

import edu.java.dto.github.EventResponse;
import edu.java.dto.github.RepositoryResponse;
import edu.java.dto.update.UpdateInfo;
import java.time.OffsetDateTime;
import java.util.List;

public interface GithubClient {
    RepositoryResponse fetchRepository(String owner, String repository);

    List<EventResponse> fetchEvents(String owner, String repository);

    UpdateInfo checkForUpdate(String url, OffsetDateTime lastUpdatedAt);
}
