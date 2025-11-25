package com.back.domain.user.user.repository;

import com.back.domain.user.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByNickname(String nickname);

    List<User> findByNicknameContainingIgnoreCaseOrBioContainingIgnoreCaseOrderByFollowersCountDesc(
            String nicknameKeyword, String bioKeyword);
}
