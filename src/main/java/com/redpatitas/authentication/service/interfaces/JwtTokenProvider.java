package com.redpatitas.authentication.service.interfaces;

import com.redpatitas.authentication.entity.UserAccount;

public interface JwtTokenProvider {

	String createAccessToken(UserAccount user);

	long getAccessTokenTtlSeconds();

	long getRefreshTokenTtlSeconds();
}
