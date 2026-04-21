package com.redpatitas.authentication.repository.interfaces;

import java.util.UUID;

/**
 * Puerto saliente para auditoría de seguridad (invoca procedimiento en BD).
 */
public interface LoginAuditRepository {

	void recordLoginAttempt(UUID userIdOrNull, String email, boolean success, String reasonOrNull);
}
