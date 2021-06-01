package main.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Builder
@AllArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //INT NOT NULL

    @Column(name = "is_moderator", nullable = false)
    private boolean isModerator; //TINYINT NOT NULL

    @Column(name = "reg_time", nullable = false)
    private LocalDateTime regTime; //DATETIME NOT NULL

    @Column(nullable = false)
    private String name; //VARCHAR(255) NOT NULL

    @Column(nullable = false)
    private String email; //VARCHAR(255) NOT NULL

    @Column(nullable = false)
    private String password; //VARCHAR(255) NOT NULL

    @Column
    private String code; //VARCHAR(255)

    @Column(columnDefinition="Text")
    private String photo; //TEXT

    @Column(name = "code_time")
    private LocalDateTime codeTime; //DATETIME

    //поля для связей

    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    public User() {
    }

    public Role getRole(){
        return isModerator ? Role.MODERATOR : Role.USER;
    }

}
