package main.api.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatisticsDTO {
/* формат
    {
        "postsCount":7,
        "likesCount":15,
        "dislikesCount":2,
        "viewsCount":58,
        "firstPublication":1590217200
    }
*/
    private int postsCount;
    private int likesCount;
    private int dislikesCount;
    private int viewsCount;
    private Long firstPublication;

}
