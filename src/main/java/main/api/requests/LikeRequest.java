package main.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LikeRequest {
    @JsonProperty("post_id")
    private Integer postId;
}
