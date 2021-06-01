package main.repository;

import main.model.Post;
import main.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    //метод получения всех видимых постов, сортировка устанавливается при вызове в Pageable (новые, старые - p.time desc, asc)
    @Query(value = "SELECT p FROM Post p " +
                   "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP " +
                   "GROUP BY p.id")
    Page<Post> findPostsOrderByTime(Pageable pageable);

    //метод получения всех постов отсортированных по количеству комментариев
    @Query(value = "SELECT p FROM Post p LEFT JOIN p.postComments comments " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP "+
            "GROUP BY p.id " +
            "ORDER BY COUNT(comments) DESC")
    Page<Post> findPostsOrderByCommentCount(Pageable pageable);

    //метод получения всех постов отсортированных по количеству лайков по убыванию, дизлайков по возрастанию
    @Query(value = "SELECT p FROM Post p LEFT JOIN p.postVotes votes " +
                   "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP "+
                   "GROUP BY p.id " +
                   "ORDER BY SUM(CASE votes.value WHEN 1 THEN 1 ELSE 0 END) DESC, " + //отдельно суммируем единички лайков
                   "SUM(CASE votes.value WHEN -1 THEN 1 ELSE 0 END) ASC") //и отдельно суммируем единички дизлайков
    Page<Post> findPostsOrderByLikes(Pageable pageable);

    //метод получения постов по запросу query
    @Query(value = "SELECT p FROM Post p " +
                   "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP " +
                                        "AND p.title LIKE %:query% GROUP BY p")
    Page<Post> findPostsByQuery(Pageable pageable, String query);

    //метод получения количества неотмодерированных постов (только активные - не черновики, включая активные посты будущей датой)
    @Query(value = "SELECT COUNT(*) FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'NEW'")
    int getModerationCount();

    //метод получения списка годов в которых были посты
    @Query(value = "SELECT YEAR(p.time) FROM Post p " +
                   "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP " +
                   "GROUP BY YEAR(p.time)")
    int[] getPostsYears();

    //метод получения количества постов по датам заданного года, формат возвращаемой строки в списке: Object[] = {2020-12-04, 7}
    @Query(value = "SELECT DATE(p.time), COUNT(*) FROM Post p " +
                   "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP " +
                                        "AND YEAR(p.time) = :year " +
                   "GROUP BY DATE(p.time)")
    List<Object[]> getDatesAndPostsCountByYear(Integer year);

    //метод получения постов по дате
    @Query(value = "SELECT p FROM Post p " +
                   "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP " +
                                        "AND DATE(p.time) = :date " +
                   "GROUP BY p")
    Page<Post> findPostsByDate(Pageable pageable, Date date);

    //метод получения постов по тегу
    @Query(value = "SELECT p FROM Post p JOIN p.tags t " +
                   "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND t.name LIKE %:tag% GROUP BY p")
    Page<Post> findPostsByTag(Pageable pageable, String tag);

    //метод получения неотмодерированных постов (только активные - не черновики, включая активные посты будущей датой)
    //а так же постов, которые были отмодерированы "лично мной"
    @Query(value = "SELECT p FROM Post p WHERE p.isActive = 1 AND (p.moderatorId = :id OR p.moderatorId = null)" +
                                                "AND p.moderationStatus = :moderationStatus")
    Page<Post> findModerationPosts(Pageable pageable, Post.ModStatus moderationStatus, User id);

    //метод получения "моих" постов в зависимости от параметров
    @Query(value = "SELECT p FROM Post p WHERE p.isActive = :isActive AND p.moderationStatus = :moderationStatus " +
                                                                       "AND p.userId = :id")
    Page<Post> findMyPosts(Pageable pageable, Post.ModStatus moderationStatus, Boolean isActive, User id);

    //метод получения лайков и дизлайков
    @Query(value="SELECT SUM(CASE votes.value WHEN 1 THEN 1 ELSE 0 END), " +
                    "SUM(CASE votes.value WHEN -1 THEN 1 ELSE 0 END) " +
                    "FROM Post p JOIN p.postVotes votes " +
                    "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP")
    List<Integer[]> getVotesStatistics();

    //метод получения суммарного количества видимых постов
    @Query(value="SELECT COUNT(*) FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP")
    int getPostCount();

    //метод получения суммарного количества просмотров видимых постов в блоге
    @Query(value="SELECT SUM(p.viewCount) FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP")
    int getViewsCount();

    //метод получения времени самого раннего поста в блоге
    @Query(value="SELECT MIN(p.time) FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP")
    LocalDateTime getOldestPostTime();

    //метод получения лайков и дизлайков конкретного пользователя
    @Query(value="SELECT SUM(CASE votes.value WHEN 1 THEN 1 ELSE 0 END), " +
            "SUM(CASE votes.value WHEN -1 THEN 1 ELSE 0 END) " +
            "FROM Post p JOIN p.postVotes votes " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time < UTC_TIMESTAMP AND p.userId = :user")
    List<Integer[]> getVotesStatistics(User user);

    //метод получения суммарного количества видимых постов конкретного пользователя
    @Query(value="SELECT COUNT(*) FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
                                                                  "AND p.time < UTC_TIMESTAMP AND p.userId = :user")
    Integer getPostCount(User user);

    //метод получения суммарного количества просмотров видимых постов в блоге конкретного пользователя
    @Query(value="SELECT SUM(p.viewCount) FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
                                                                "AND p.time < UTC_TIMESTAMP AND p.userId = :user")
    Integer getViewsCount(User user);

    //метод получения времени самого раннего поста в блоге конкретного пользователя
    @Query(value="SELECT MIN(p.time) FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
                                                            "AND p.time < UTC_TIMESTAMP AND p.userId = :user")
    LocalDateTime getOldestPostTime(User user);

}
