package main.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class RegisterRequest {
    @JsonProperty("e_mail")
    String email;
    String password;
    String name;
    String captcha;
    @JsonProperty("captcha_secret")
    String captchaSecret;
    String code;
}
