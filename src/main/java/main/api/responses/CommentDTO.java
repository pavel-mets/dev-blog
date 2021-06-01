package main.api.responses;

import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@Builder
public class CommentDTO {

    int id;
    long timestamp;
    String text;
    User user;

    @Value
    @Builder
    public static class User {
        int id;
        String name;
        String photo;
    }
}
