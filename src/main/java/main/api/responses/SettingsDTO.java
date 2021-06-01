package main.api.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class SettingsDTO {
/* формат:
    {
     "MULTIUSER_MODE": false,
     "POST_PREMODERATION": true,
     "STATISTICS_IS_PUBLIC": true
    }
*/

    @JsonProperty("MULTIUSER_MODE")
    private boolean multiuserMode;

    @JsonProperty("POST_PREMODERATION")
    private boolean postPremoderation;

    @JsonProperty("STATISTICS_IS_PUBLIC")
    private boolean statisticIsPublic;
}