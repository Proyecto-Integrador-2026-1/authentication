package com.redpatitas.authentication.service.interfaces;

public interface PasswordHashService {

	String encode(String rawPassword);

	boolean matches(String rawPassword, String encodedPassword);
}
