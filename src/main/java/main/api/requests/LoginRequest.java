package main.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class LoginRequest {
    @JsonProperty("e_mail")
    String email;
    String password;

}
