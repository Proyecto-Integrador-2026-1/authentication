package com.redpatitas.authentication.repository.impl;

import java.util.List;

import org.springframework.stereotype.Component;

import com.redpatitas.authentication.entity.RoleUserCount;
import com.redpatitas.authentication.repository.interfaces.UserJpaRepository;
import com.redpatitas.authentication.repository.interfaces.UserRoleStatsRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRoleStatsRepositoryImpl implements UserRoleStatsRepository {

	private final UserJpaRepository userJpaRepository;

	@Override
	public List<RoleUserCount> countUsersGroupedByRole() {
		return userJpaRepository.countUsersGroupedByRole().stream()
				.map(row -> new RoleUserCount((String) row[0], ((Number) row[1]).longValue()))
				.toList();
	}
}
