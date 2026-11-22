package com.ferreteria.ms_auth.repository;

import com.ferreteria.ms_auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.token = :token AND rt.revokedAt IS NULL")
    int revokeByToken(@Param("token") String token, @Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.userId = :userId AND rt.revokedAt IS NULL")
    int revokeAllByUserId(@Param("userId") Long userId, @Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now OR rt.revokedAt IS NOT NULL")
    int deleteExpiredOrRevoked(@Param("now") Instant now);

    boolean existsByToken(String token);
}