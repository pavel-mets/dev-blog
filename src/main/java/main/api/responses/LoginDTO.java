package main.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginDTO {
/* формат:
    {
     "result": true,
     "user": {
         "id": 576,
         "name": "Дмитрий Петров",
         "photo": "/avatars/ab/cd/ef/52461.jpg",
         "email": "my@email.com",
         "moderation": true,
         "moderationCount": 0,
         "settings": true
         }
    }
 */
    private boolean result;
    private User user;

    @Value
    @Builder
    public static class User {
        int id;
        String name;
        String photo;
        String email;
        boolean moderation;
        int moderationCount;
        boolean settings;
    }
}