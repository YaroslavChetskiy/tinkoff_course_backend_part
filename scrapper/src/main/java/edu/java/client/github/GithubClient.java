package edu.java.client.github;

import edu.java.dto.entity.Link;
import edu.java.dto.github.RepositoryResponse;
import java.time.OffsetDateTime;

public interface GithubClient {
    RepositoryResponse fetchRepository(String owner, String repository);

    OffsetDateTime checkForUpdate(Link link);
}
