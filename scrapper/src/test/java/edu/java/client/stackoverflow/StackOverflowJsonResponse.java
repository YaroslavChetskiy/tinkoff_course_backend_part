package edu.java.client.stackoverflow;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StackOverflowJsonResponse {

    public static final String RESPONSE_BODY = """
        {
          "items": [
            {
              "tags": [
                "c#"
              ],
              "owner": {
                "account_id": 3078547,
                "reputation": 465,
                "user_id": 2607332,
                "user_type": "registered",
                "accept_rate": 50,
                "profile_image": "https://graph.facebook.com/517527802/picture?type=large",
                "display_name": "Simon Price",
                "link": "https://stackoverflow.com/users/2607332/simon-price"
              },
              "is_answered": true,
              "view_count": 474,
              "closed_date": 1409687590,
              "accepted_answer_id": 25630325,
              "answer_count": 2,
              "score": 0,
              "last_activity_date": 1519360722,
              "creation_date": 1409683372,
              "last_edit_date": 1519360722,
              "question_id": 25630159,
              "link": "https://stackoverflow.com/questions/25630159/connect-to-stack-overflow-api",
              "closed_reason": "Not suitable for this site",
              "title": "Connect to Stack Overflow API"
            }
          ],
          "has_more": false,
          "quota_max": 10000,
          "quota_remaining": 9957
        }
        """;
}
