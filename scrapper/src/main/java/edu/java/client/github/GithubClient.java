package edu.java.client.github;

import edu.java.dto.github.RepositoryResponse;

public interface GithubClient {
    RepositoryResponse fetchRepository(String owner, String repository);
}
