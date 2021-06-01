package main.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //INT NOT NULL

    @Column(nullable = false)
    private String name; //VARCHAR(255) NOT NULL

    @ManyToMany
    @JoinTable(name = "Tag2Post",
            joinColumns = {@JoinColumn(name = "tag_id")},
            inverseJoinColumns = {@JoinColumn(name = "post_id")}
    )
    private List<Post> posts;

    @OneToMany(mappedBy = "tag")
    private List<Tag2Post> tag2posts;

    public Tag(String name) {
        this.name = name;
    }

}
