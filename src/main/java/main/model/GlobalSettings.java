package main.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "global_settings")
@Data
@NoArgsConstructor
public class GlobalSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id; //INT NOT NULL

    @Column(nullable = false)
    private String code; //VARCHAR(255) NOT NULL

    @Column(nullable = false)
    private String name; //VARCHAR(255) NOT NULL

    @Column(nullable = false)
    private String value; //VARCHAR(255) NOT NULL
}
