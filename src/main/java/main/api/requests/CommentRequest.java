package main.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CommentRequest {
    @JsonProperty("parent_id")
    Integer parentId;
    @JsonProperty("post_id")
    Integer postId;
    String text;
}
