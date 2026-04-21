package com.redpatitas.authentication.service.impl;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redpatitas.authentication.service.interfaces.LoginUseCase.LoginResult;
import com.redpatitas.authentication.service.interfaces.RefreshTokenUseCase;
import com.redpatitas.authentication.service.interfaces.JwtTokenProvider;
import com.redpatitas.authentication.service.interfaces.RefreshTokenIssuer;
import com.redpatitas.authentication.exception.AuthenticationDomainException;
import com.redpatitas.authentication.repository.interfaces.RefreshTokenRepository;
import com.redpatitas.authentication.repository.interfaces.UserRepository;
import com.redpatitas.authentication.entity.UserAccount;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

	private final UserRepository users;
	private final RefreshTokenRepository refreshTokens;
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenIssuer refreshTokenIssuer;
	private final Clock clock;

	@Override
	@Transactional
	public LoginResult refresh(RefreshCommand command) {
		if (command.rawRefreshToken() == null || command.rawRefreshToken().isBlank()) {
			throw new AuthenticationDomainException("INVALID_REFRESH", "Refresh token requerido");
		}
		Instant now = clock.instant();
		String hash = refreshTokenIssuer.sha256Hex(command.rawRefreshToken().trim());
		var active = refreshTokens.findActiveByTokenHash(hash, now)
				.orElseThrow(() -> new AuthenticationDomainException("INVALID_REFRESH", "Refresh token inválido o revocado"));

		UserAccount user = users.findById(active.userId())
				.orElseThrow(() -> new AuthenticationDomainException("INVALID_REFRESH", "Usuario no encontrado"));

		if (!user.isEnabled()) {
			throw new AuthenticationDomainException("AUTH_ACCOUNT_DISABLED", "Cuenta deshabilitada");
		}
		if (user.isLocked(now)) {
			throw new AuthenticationDomainException("AUTH_ACCOUNT_LOCKED", "Cuenta bloqueada temporalmente");
		}

		refreshTokens.revokeById(active.id());

		String access = jwtTokenProvider.createAccessToken(user);
		String newRefreshPlain = refreshTokenIssuer.newOpaqueToken();
		Instant refreshExp = now.plusSeconds(jwtTokenProvider.getRefreshTokenTtlSeconds());
		refreshTokens.save(user.getId(), refreshTokenIssuer.sha256Hex(newRefreshPlain), refreshExp);

		return new LoginResult(
				access,
				"Bearer",
				jwtTokenProvider.getAccessTokenTtlSeconds(),
				user.getRoles(),
				newRefreshPlain,
				jwtTokenProvider.getRefreshTokenTtlSeconds());
	}
}
