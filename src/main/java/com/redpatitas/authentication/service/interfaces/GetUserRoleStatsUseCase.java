package com.redpatitas.authentication.service.interfaces;

import java.util.List;

import com.redpatitas.authentication.entity.RoleUserCount;

public interface GetUserRoleStatsUseCase {

	List<RoleUserCount> getUsersByRole();
}
