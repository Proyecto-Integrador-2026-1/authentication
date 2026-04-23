package com.redpatitas.authentication.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import com.redpatitas.authentication.exception.AuthenticationDomainException;
import com.redpatitas.authentication.repository.interfaces.UserRepository;
import com.redpatitas.authentication.service.interfaces.PasswordHashService;
import com.redpatitas.authentication.service.interfaces.RegisterUserUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

	private static final String DEFAULT_ROLE = "ROLE_USER";

	private final UserRepository users;
	private final PasswordHashService passwordHashService;

	@Override
	@Transactional
	public RegisterResult register(RegisterCommand command) {
		String nombre = command.nombre().trim();
		String apellido = command.apellido().trim();
		String telefono = normalizeTelefono(command.telefono());
		String email = command.email().trim().toLowerCase();

		if (telefono.isBlank()) {
			throw new AuthenticationDomainException("AUTH_PHONE_INVALID", "El teléfono debe contener solo números");
		}

		if (users.existsByEmail(email)) {
			throw new AuthenticationDomainException("AUTH_EMAIL_ALREADY_EXISTS", "El correo ya está registrado");
		}
		if (users.existsByTelefono(telefono)) {
			throw new AuthenticationDomainException("AUTH_PHONE_ALREADY_EXISTS", "El teléfono ya está registrado");
		}

		String passwordHash = passwordHashService.encode(command.rawPassword());
		var userId = createUserWithIntegrityMapping(nombre, apellido, telefono, email, passwordHash);

		return new RegisterResult(userId.toString(), email, nombre, apellido, telefono);
	}

	private java.util.UUID createUserWithIntegrityMapping(
			String nombre,
			String apellido,
			String telefono,
			String email,
			String passwordHash) {
		try {
			return users.createUser(nombre, apellido, telefono, email, passwordHash, DEFAULT_ROLE);
		}
		catch (DataIntegrityViolationException ex) {
			String details = ex.getMostSpecificCause() != null
					? ex.getMostSpecificCause().getMessage().toLowerCase()
					: "";
			if (details.contains("telefono")) {
				throw new AuthenticationDomainException("AUTH_PHONE_ALREADY_EXISTS", "El teléfono ya está registrado");
			}
			if (details.contains("email") || details.contains("auth_users_email") || details.contains("ux_auth_users_email_lower")) {
				throw new AuthenticationDomainException("AUTH_EMAIL_ALREADY_EXISTS", "El correo ya está registrado");
			}
			throw new AuthenticationDomainException("AUTH_DUPLICATE_DATA", "El usuario ya existe");
		}
	}

	private static String normalizeTelefono(String rawTelefono) {
		String digitsOnly = rawTelefono == null ? "" : rawTelefono.replaceAll("\\D", "");
		while (digitsOnly.length() > 10 && digitsOnly.startsWith("57")) {
			digitsOnly = digitsOnly.substring(2);
		}
		return digitsOnly;
	}
}
