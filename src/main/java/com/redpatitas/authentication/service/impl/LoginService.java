package com.redpatitas.authentication.service.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redpatitas.authentication.service.interfaces.LoginUseCase;
import com.redpatitas.authentication.service.interfaces.JwtTokenProvider;
import com.redpatitas.authentication.service.interfaces.PasswordHashService;
import com.redpatitas.authentication.service.interfaces.RefreshTokenIssuer;
import com.redpatitas.authentication.exception.AuthenticationDomainException;
import com.redpatitas.authentication.repository.interfaces.LoginAuditRepository;
import com.redpatitas.authentication.repository.interfaces.RefreshTokenRepository;
import com.redpatitas.authentication.repository.interfaces.UserRepository;
import com.redpatitas.authentication.entity.UserAccount;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginUseCase {

	private static final int MAX_FAILED_ATTEMPTS = 5;
	private static final int LOCK_MINUTES = 15;

	private final UserRepository users;
	private final PasswordHashService passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final LoginAuditRepository loginAudit;
	private final RefreshTokenRepository refreshTokens;
	private final RefreshTokenIssuer refreshTokenIssuer;
	private final Clock clock;

	@Override
	@Transactional
	public LoginResult login(LoginCommand command) {
		Instant now = clock.instant();
		var userOpt = users.findByEmail(command.email().trim().toLowerCase());

		if (userOpt.isEmpty()) {
			loginAudit.recordLoginAttempt(null, command.email(), false, "USER_NOT_FOUND");
			throw new AuthenticationDomainException("AUTH_INVALID_CREDENTIALS", "Credenciales inválidas");
		}

		UserAccount user = userOpt.get();

		if (!user.isEnabled()) {
			loginAudit.recordLoginAttempt(user.getId(), user.getEmail(), false, "USER_DISABLED");
			throw new AuthenticationDomainException("AUTH_ACCOUNT_DISABLED", "Cuenta deshabilitada");
		}

		if (user.isLocked(now)) {
			loginAudit.recordLoginAttempt(user.getId(), user.getEmail(), false, "ACCOUNT_LOCKED");
			throw new AuthenticationDomainException("AUTH_ACCOUNT_LOCKED", "Cuenta bloqueada temporalmente");
		}

		boolean passwordOk = passwordEncoder.matches(command.rawPassword(), user.getPasswordHash());
		if (!passwordOk) {
			int next = user.getFailedLoginAttempts() + 1;
			Instant lockUntil = null;
			if (next >= MAX_FAILED_ATTEMPTS) {
				lockUntil = now.plus(LOCK_MINUTES, ChronoUnit.MINUTES);
			}
			users.registerFailedLogin(user.getId(), next, lockUntil);
			loginAudit.recordLoginAttempt(user.getId(), user.getEmail(), false, "BAD_PASSWORD");
			throw new AuthenticationDomainException("AUTH_INVALID_CREDENTIALS", "Credenciales inválidas");
		}

		users.resetFailedLogin(user.getId());
		String token = jwtTokenProvider.createAccessToken(user);
		loginAudit.recordLoginAttempt(user.getId(), user.getEmail(), true, null);

		refreshTokens.revokeAllForUser(user.getId());
		String refreshPlain = refreshTokenIssuer.newOpaqueToken();
		Instant refreshExp = now.plusSeconds(jwtTokenProvider.getRefreshTokenTtlSeconds());
		refreshTokens.save(user.getId(), refreshTokenIssuer.sha256Hex(refreshPlain), refreshExp);

		return new LoginResult(
				token,
				"Bearer",
				jwtTokenProvider.getAccessTokenTtlSeconds(),
				user.getRoles(),
				refreshPlain,
				jwtTokenProvider.getRefreshTokenTtlSeconds());
	}
}
