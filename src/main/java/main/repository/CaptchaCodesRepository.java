package main.repository;

import main.model.CaptchaCodes;
import main.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaptchaCodesRepository extends JpaRepository<CaptchaCodes, Integer> {

    @Query(value = "SELECT c FROM CaptchaCodes c WHERE TIMESTAMPDIFF(MINUTE, c.time, UTC_TIMESTAMP) > :captchaExpires")
    List<CaptchaCodes> getExpiredCaptcha(int captchaExpires);

    Optional<CaptchaCodes> findBySecretCode(String secretCode);
}
