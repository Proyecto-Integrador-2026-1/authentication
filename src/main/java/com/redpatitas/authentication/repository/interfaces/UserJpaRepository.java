package com.redpatitas.authentication.repository.interfaces;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.redpatitas.authentication.entity.UserEntity;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {

	Optional<UserEntity> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByTelefono(String telefono);

	/**
	 * Consulta no trivial: join usuario-roles, agregación y agrupación (reporte operativo).
	 */
	@Query("select r.name, count(distinct u.id) from UserEntity u join u.roles r group by r.name order by r.name")
	List<Object[]> countUsersGroupedByRole();

	@Modifying
	@Query("update UserEntity u set u.failedLoginAttempts = 0, u.lockedUntil = null where u.id = :id")
	void resetFailedLogin(@Param("id") UUID id);

	@Modifying
	@Query("update UserEntity u set u.failedLoginAttempts = :attempts, u.lockedUntil = :lockedUntil where u.id = :id")
	void updateFailedLogin(
			@Param("id") UUID id,
			@Param("attempts") int attempts,
			@Param("lockedUntil") java.time.Instant lockedUntil);
}
