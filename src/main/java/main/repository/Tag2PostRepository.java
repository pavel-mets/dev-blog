package main.repository;

import main.model.Post;
import main.model.Tag;
import main.model.Tag2Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Tag2PostRepository extends JpaRepository<Tag2Post, Integer> {

    //запрос на выборку по Post и Tag
    Tag2Post findByPostAndTag(Post post, Tag tag);

}
