package main.repository;

import main.model.CaptchaCodes;
import main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByCode(String code);

    @Query(value = "SELECT u FROM User u WHERE u.codeTime IS NOT NULL AND TIMESTAMPDIFF(MINUTE, u.codeTime, UTC_TIMESTAMP) > :restoreCodeExpires")
    List<User> getUsersByExpiredCode(int restoreCodeExpires);
}
