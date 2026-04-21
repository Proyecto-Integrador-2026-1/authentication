package com.redpatitas.authentication.service.impl;

import org.springframework.stereotype.Component;
import com.redpatitas.authentication.service.interfaces.RefreshTokenIssuer;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefreshTokenIssuerImpl implements RefreshTokenIssuer {

	private final OpaqueRefreshTokenGenerator generator;

	@Override
	public String newOpaqueToken() {
		return generator.generate();
	}

	@Override
	public String sha256Hex(String plainToken) {
		return Sha256Hex.of(plainToken);
	}
}
