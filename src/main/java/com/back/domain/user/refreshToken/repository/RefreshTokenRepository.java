package com.back.domain.user.refreshToken.repository;

import com.back.domain.user.refreshToken.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
}
