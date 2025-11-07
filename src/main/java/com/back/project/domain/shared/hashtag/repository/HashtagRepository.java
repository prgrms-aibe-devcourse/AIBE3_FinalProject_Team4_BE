package  com.back.project.domain.shared.hashtag.repository;

import com.back.project.domain.shared.hashtag.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);

    boolean existsByName(String name);
}