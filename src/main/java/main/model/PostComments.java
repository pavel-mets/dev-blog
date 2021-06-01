package main.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_comments")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PostComments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //INT NOT NULL

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private PostComments parentId; //INT

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime time; //DATETIME NOT NULL

    @Column(columnDefinition="Text", nullable = false)
    private String text; //TEXT NOT NULL

}
