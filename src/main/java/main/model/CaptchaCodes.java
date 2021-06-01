package main.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "captcha_codes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaCodes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //INT NOT NULL

    @Column(nullable = false)
    private LocalDateTime time; //DATETIME NOT NULL

    @Column(columnDefinition = "Tinytext", nullable = false)
    private String code; //TINYTEXT NOT NULL

    @Column(name = "secret_code", columnDefinition = "Tinytext", nullable = false)
    private String secretCode; //TINYTEXT NOT NULL
}
