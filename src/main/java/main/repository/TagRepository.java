package main.repository;

import main.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

    //запрос тегов по query, формат возвращаемой строки в списке Object[] = {Tag, количество}
    @Query(value="SELECT t.name, COUNT(*) FROM Tag t JOIN t.posts p " +
                 "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP " +
                                      "AND t.name LIKE %:query% " +
                 "GROUP BY t.name")
    List<Object[]> getTagsByQuery(String query);

    //запрос всех тегов
    @Query(value="SELECT t.name, COUNT(*) FROM Tag t JOIN t.posts p " +
                 "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP " +
                 "GROUP BY t.name")
    List<Object[]> getAllTags();

    //количество всех видимых публикаций
    @Query(value = "SELECT COUNT(*) FROM Post p " +
                   "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP")
    Integer getVisiblePostsCount();

    //запрос тега по имени
    Optional<Tag> findByName(String tagName);

}
