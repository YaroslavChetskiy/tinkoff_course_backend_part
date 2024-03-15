package edu.java.client.github;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GithubJsonResponse {

    public static final String REPO_RESPONSE_BODY = """
        {
          "id": 752229016,
          "node_id": "R_kgDOLNYamA",
          "name": "tinkoff_course_backend_part",
          "full_name": "YaroslavChetskiy/tinkoff_course_backend_part",
          "html_url": "https://github.com/YaroslavChetskiy/tinkoff_course_backend_part",
          "created_at": "2024-02-03T12:17:54Z",
          "updated_at": "2024-02-03T12:20:34Z",
          "pushed_at": "2024-02-15T18:32:44Z"
        }
        """;

    public static final String EVENTS_RESPONSE_BODY = """
        [
            {
          "id": "36447305530",
          "type": "PushEvent",
          "payload": {
            "repository_id": 752229016,
            "push_id": 17492474866,
            "size": 1,
            "distinct_size": 1,
            "ref": "refs/heads/homework5",
            "head": "6287a0ca0c455617313ed534a9eb835cc7d722c2",
            "before": "aa4595837d4e233a208c99a8dfd6667640ce70b8",
            "commits": [
              {
                "message": "Fixes and refactoring after code review",
                "distinct": true,
                "url": "https://api.github.com/repos/YaroslavChetskiy/tinkoff_course_backend_part/commits/6287a0ca0c455617313ed534a9eb835cc7d722c2"
              }
            ]
          },
          "created_at": "2024-03-11T18:51:37Z"
            },
            {
            "id": "36399534176",
            "type": "IssueCommentEvent",
            "payload": {
              "action": "created",
              "issue": {
                "url": "https://api.github.com/repos/YaroslavChetskiy/tinkoff_course_backend_part/issues/5",
                "number": 5,
                "title": "Hw5",
                "created_at": "2024-03-09T17:46:03Z",
                "updated_at": "2024-03-09T17:46:49Z"
              },
              "comment": {
                "url": "https://api.github.com/repos/YaroslavChetskiy/tinkoff_course_backend_part/issues/comments/1986928784",
                "created_at": "2024-03-09T17:46:47Z",
                "updated_at": "2024-03-09T17:46:47Z",
                "author_association": "NONE",
                "body": "Some body"
              }
            },
            "public": true,
            "created_at": "2024-03-09T17:46:49Z"
            },
            {
            "id": "36399527513",
            "type": "PullRequestEvent",
            "actor": {
              "id": 63596658,
              "login": "YaroslavChetskiy",
              "display_login": "YaroslavChetskiy",
              "gravatar_id": "",
              "url": "https://api.github.com/users/YaroslavChetskiy",
              "avatar_url": "https://avatars.githubusercontent.com/u/63596658?"
            },
            "repo": {
              "id": 752229016,
              "name": "YaroslavChetskiy/tinkoff_course_backend_part",
              "url": "https://api.github.com/repos/YaroslavChetskiy/tinkoff_course_backend_part"
            },
            "payload": {
              "action": "opened",
              "number": 5,
              "pull_request": {
                "url": "https://api.github.com/repos/YaroslavChetskiy/tinkoff_course_backend_part/pulls/5",
                "id": 1764127763,
                "number": 5,
                "state": "open",
                "locked": false,
                "title": "Hw5",
                "body": null,
                "created_at": "2024-03-09T17:46:03Z",
                "updated_at": "2024-03-09T17:46:03Z",
                "closed_at": null
              }
            },
            "public": true,
            "created_at": "2024-03-09T17:46:04Z"
           }
        ]
        """;
}
