package main.api.requests;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class ProfileRequest {
    MultipartFile photoFile;
    String photo;
    String name;
    String email;
    String password;
    Integer removePhoto;
}
