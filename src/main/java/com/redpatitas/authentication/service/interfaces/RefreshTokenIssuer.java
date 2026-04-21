package com.redpatitas.authentication.service.interfaces;

/**
 * Generación de refresh tokens opacos y hash para persistencia (detalle en adaptador).
 */
public interface RefreshTokenIssuer {

	String newOpaqueToken();

	String sha256Hex(String plainToken);
}
