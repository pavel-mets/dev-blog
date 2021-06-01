package main.api.responses;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class TagsDTO {
/* формат:
    {
     "tags":
         [
         {"name":"Java", "weight":1},
         {"name":"Spring", "weight":0.56},
         {"name":"Hibernate", "weight":0.22},
         {"name":"Hadoop", "weight":0.17},
         ]
    }
*/
    List<Tag> tags = new ArrayList<>();

    @Value
    @Builder
    public static class Tag {
        String name;
        float weight;
    }
}
