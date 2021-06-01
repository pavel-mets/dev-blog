package main.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostsDTO {

    private long count;
    private List<Post> posts = new ArrayList<>();

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Post {
        Integer id;
        Long timestamp;
        Boolean active; // отказ от примитива по причине необходимости null для JSON
        User user;
        String title;
        String announce; //имеет значение (не null) только для постов в списке
        String text; //имеет значение (не null) только для вывода одного поста
        Long likeCount;
        Long dislikeCount;
        Integer commentCount;
        Integer viewCount;
        List<CommentDTO> comments; //имеет значение (не null) только для вывода одного поста
        List<String> tags; //имеет значение (не null) только для вывода одного поста

    }

    @Value
    @Builder
    public static class User {
        int id;
        String name;
    }



}