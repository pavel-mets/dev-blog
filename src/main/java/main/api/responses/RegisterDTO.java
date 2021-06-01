package main.api.responses;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterDTO {
/*
    {
     "result": false,
     "errors": {
         "email": "Этот e-mail уже зарегистрирован",
         "name": "Имя указано неверно",
         "password": "Пароль короче 6-ти символов",
         "captcha": "Код с картинки введён неверно"
         }
    }
*/

    private boolean result;
    private Errors errors;

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Errors {
        @JsonProperty("e_mail")
        String email;
        String name;
        String password;
        String captcha;
        String photo;
        String code;

    }
}
