package com.redpatitas.authentication.repository.interfaces;

import java.util.List;

import com.redpatitas.authentication.entity.RoleUserCount;

public interface UserRoleStatsRepository {

	List<RoleUserCount> countUsersGroupedByRole();
}
