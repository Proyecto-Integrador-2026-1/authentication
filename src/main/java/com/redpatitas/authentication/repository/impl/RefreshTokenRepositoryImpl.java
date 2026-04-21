package com.redpatitas.authentication.repository.impl;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.redpatitas.authentication.entity.RefreshTokenEntity;
import com.redpatitas.authentication.repository.interfaces.RefreshTokenJpaRepository;
import com.redpatitas.authentication.repository.interfaces.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

	private final RefreshTokenJpaRepository jpaRepository;
	private final Clock clock;

	@Override
	public Optional<RefreshTokenActive> findActiveByTokenHash(String sha256Hex, Instant now) {
		return jpaRepository.findActiveByHash(sha256Hex, now)
				.map(e -> new RefreshTokenActive(e.getId(), e.getUserId()));
	}

	@Override
	public void revokeAllForUser(UUID userId) {
		jpaRepository.revokeAllActiveByUserId(userId);
	}

	@Override
	public UUID save(UUID userId, String tokenSha256Hex, Instant expiresAt) {
		RefreshTokenEntity e = RefreshTokenEntity.create(
			UUID.randomUUID(),
			userId,
			tokenSha256Hex,
			expiresAt,
			clock.instant()
		);
    return jpaRepository.save(e).getId();
	}

	@Override
	public void revokeById(UUID id) {
		jpaRepository.findById(id).ifPresent(e -> {
			e.revoke();
			jpaRepository.save(e);
		});
	}
}