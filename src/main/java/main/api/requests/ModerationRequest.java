package main.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class ModerationRequest {
    @JsonProperty("post_id")
    Integer postId;
    String decision;
}
