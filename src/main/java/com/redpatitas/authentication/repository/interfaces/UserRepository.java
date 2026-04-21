package com.redpatitas.authentication.repository.interfaces;

import java.util.Optional;
import java.util.UUID;

import com.redpatitas.authentication.entity.UserAccount;

public interface UserRepository {

	Optional<UserAccount> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByTelefono(String telefono);

	UUID createUser(String nombre, String apellido, String telefono, String email, String passwordHash, String roleName);

	Optional<UserAccount> findById(UUID id);

	void resetFailedLogin(UUID userId);

	void registerFailedLogin(UUID userId, int newAttemptCount, java.time.Instant lockedUntilOrNull);
}
