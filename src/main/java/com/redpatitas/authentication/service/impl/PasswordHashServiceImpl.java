package com.redpatitas.authentication.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.redpatitas.authentication.service.interfaces.PasswordHashService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PasswordHashServiceImpl implements PasswordHashService {

	private final PasswordEncoder delegate;

	@Override
	public String encode(String rawPassword) {
		return delegate.encode(rawPassword);
	}

	@Override
	public boolean matches(String rawPassword, String encodedPassword) {
		return delegate.matches(rawPassword, encodedPassword);
	}
}
