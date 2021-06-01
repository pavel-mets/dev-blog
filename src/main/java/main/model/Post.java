package main.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //INT NOT NULL

    @Column(name = "is_active", nullable = false)
    private boolean isActive; //TINYINT NOT NULL

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", columnDefinition = "enum('NEW', 'ACCEPTED', 'DECLINED') default 'NEW'", nullable = false)
    private ModStatus moderationStatus; //ENUM NOT NULL DEFAULT = NEW

    @ManyToOne
    @JoinColumn(name = "moderator_id")
    private User moderatorId; //INT

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User userId; //INT NOT NULL

    @Column(nullable = false)
    private LocalDateTime time; //DATETIME NOT NULL

    @Column(nullable = false)
    private String title; //VARCHAR(255) NOT NULL

    @Column(columnDefinition = "Text", nullable = false)
    private String text; //TEXT NOT NULL

    @Column(name = "view_count", nullable = false)
    private int viewCount; //INT NOT NULL

    //поля для связей
    //связь с таблицей комментариев, удаляем комментарии к постам при удалении постов
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComments> postComments;

    //связь с таблицей лайков, удаляем лайки и дизлайки при удалении поста
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostVote> postVotes;

    //связь с таблицей тегов
    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "posts")
    private List<Tag> tags;

    //связь с таблицей Tag2Post
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tag2Post> tag2Posts;

    public enum ModStatus {NEW, ACCEPTED, DECLINED}

}