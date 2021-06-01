package main.api.responses;

import lombok.Data;
import java.util.TreeMap;

@Data
public class CalendarDTO {
/* формат:
    {
     "years": [2017, 2018, 2019, 2020],
         "posts": {
         "2019-12-17": 56,
         "2019-12-14": 11,
         "2019-06-17": 1,
         "2020-03-12": 6
         }
    }
*/
    private int[] years;
    private TreeMap<String, Long> posts = new TreeMap<>();
}