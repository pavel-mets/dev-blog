package main.api.responses;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class InitDTO {
/* формат:
    {
     "title": "DevPub",
     "subtitle": "Рассказы разработчиков",
     "phone": "+7 903 666-44-55",
     "email": "mail@mail.ru",
     "copyright": "Дмитрий Сергеев",
     "copyrightFrom": "2005"
    }
*/
    @Value("${blog.title}")
    private String title;
    @Value("${blog.subtitle}")
    private String subtitle;
    @Value("${blog.phone}")
    private String phone;
    @Value("${blog.copyright}")
    private String copyright;
    @Value("${blog.copyrightFrom}")
    private String copyrightFrom;
}
