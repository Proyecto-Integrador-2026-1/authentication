package com.redpatitas.authentication.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.redpatitas.authentication.service.interfaces.GetUserRoleStatsUseCase;
import com.redpatitas.authentication.entity.RoleUserCount;
import com.redpatitas.authentication.repository.interfaces.UserRoleStatsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRoleStatsService implements GetUserRoleStatsUseCase {

	private final UserRoleStatsRepository userRoleStatsRepository;

	@Override
	public List<RoleUserCount> getUsersByRole() {
		return userRoleStatsRepository.countUsersGroupedByRole();
	}
}
