package com.redpatitas.authentication.repository.interfaces;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.redpatitas.authentication.entity.RoleEntity;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, UUID> {

	Optional<RoleEntity> findByName(String name);
}
