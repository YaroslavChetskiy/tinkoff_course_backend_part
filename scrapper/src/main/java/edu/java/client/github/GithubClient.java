package edu.java.client.github;

import edu.java.dto.entity.Link;
import edu.java.dto.github.EventResponse;
import edu.java.dto.github.RepositoryResponse;
import edu.java.dto.update.UpdateInfo;
import java.util.List;

public interface GithubClient {
    RepositoryResponse fetchRepository(String owner, String repository);

    List<EventResponse> fetchEvents(String owner, String repository);

    UpdateInfo checkForUpdate(Link link);
}
