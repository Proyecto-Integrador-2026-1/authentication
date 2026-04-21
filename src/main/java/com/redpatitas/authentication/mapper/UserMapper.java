package com.redpatitas.authentication.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.redpatitas.authentication.entity.UserAccount;
import com.redpatitas.authentication.entity.UserEntity;

@Component
public class UserMapper {

	public UserAccount toDomain(UserEntity entity) {
		var roles = entity.getRoles().stream()
				.map(r -> r.getName())
				.collect(Collectors.toUnmodifiableSet());
		return UserAccount.builder()
				.id(entity.getId())
				.email(entity.getEmail())
				.passwordHash(entity.getPasswordHash())
				.roles(roles)
				.enabled(entity.isEnabled())
				.failedLoginAttempts(entity.getFailedLoginAttempts())
				.lockedUntil(entity.getLockedUntil())
				.build();
	}
}
