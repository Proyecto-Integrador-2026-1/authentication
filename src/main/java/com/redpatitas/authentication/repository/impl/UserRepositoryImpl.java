package com.redpatitas.authentication.repository.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.redpatitas.authentication.exception.AuthenticationDomainException;
import com.redpatitas.authentication.entity.RoleEntity;
import com.redpatitas.authentication.entity.UserAccount;
import com.redpatitas.authentication.entity.UserEntity;
import com.redpatitas.authentication.mapper.UserMapper;
import com.redpatitas.authentication.repository.interfaces.RoleJpaRepository;
import com.redpatitas.authentication.repository.interfaces.UserJpaRepository;
import com.redpatitas.authentication.repository.interfaces.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	private final UserJpaRepository userJpaRepository;
	private final RoleJpaRepository roleJpaRepository;
	private final UserMapper userMapper;

	@Override
	public Optional<UserAccount> findByEmail(String email) {
		return userJpaRepository.findByEmailIgnoreCase(email).map(userMapper::toDomain);
	}

	@Override
	public boolean existsByEmail(String email) {
		return userJpaRepository.existsByEmailIgnoreCase(email);
	}

	@Override
	public boolean existsByTelefono(String telefono) {
		return userJpaRepository.existsByTelefono(telefono);
	}

	@Override
	public UUID createUser(String nombre, String apellido, String telefono, String email, String passwordHash, String roleName) {
		RoleEntity role = roleJpaRepository.findByName(roleName)
				.orElseThrow(() -> new AuthenticationDomainException("AUTH_ROLE_NOT_FOUND", "Rol no configurado: " + roleName));

		UserEntity user = new UserEntity();
		user.setNombre(nombre);
		user.setApellido(apellido);
		user.setTelefono(telefono);
		user.setEmail(email);
		user.setPasswordHash(passwordHash);
		user.setEnabled(true);
		user.setFailedLoginAttempts(0);
		user.getRoles().add(role);

		return userJpaRepository.save(user).getId();
	}

	@Override
	public Optional<UserAccount> findById(UUID id) {
		return userJpaRepository.findById(id).map(userMapper::toDomain);
	}

	@Override
	public void resetFailedLogin(UUID userId) {
		userJpaRepository.resetFailedLogin(userId);
	}

	@Override
	public void registerFailedLogin(UUID userId, int newAttemptCount, java.time.Instant lockedUntilOrNull) {
		userJpaRepository.updateFailedLogin(userId, newAttemptCount, lockedUntilOrNull);
	}
}
