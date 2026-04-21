package com.redpatitas.authentication.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.redpatitas.authentication.exception.AuthenticationDomainException;
import com.redpatitas.authentication.repository.interfaces.UserRepository;
import com.redpatitas.authentication.service.interfaces.PasswordHashService;
import com.redpatitas.authentication.service.interfaces.RegisterUserUseCase;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterUserService implements RegisterUserUseCase {

	private static final String DEFAULT_ROLE = "ROLE_OPERATOR";

	private final UserRepository users;
	private final PasswordHashService passwordHashService;

	@Override
	@Transactional
	public RegisterResult register(RegisterCommand command) {
		String nombre = command.nombre().trim();
		String apellido = command.apellido().trim();
		String telefono = command.telefono().trim();
		String email = command.email().trim().toLowerCase();

		if (users.existsByEmail(email)) {
			throw new AuthenticationDomainException("AUTH_EMAIL_ALREADY_EXISTS", "El correo ya está registrado");
		}
		if (users.existsByTelefono(telefono)) {
			throw new AuthenticationDomainException("AUTH_PHONE_ALREADY_EXISTS", "El teléfono ya está registrado");
		}

		String passwordHash = passwordHashService.encode(command.rawPassword());
		var userId = users.createUser(nombre, apellido, telefono, email, passwordHash, DEFAULT_ROLE);

		return new RegisterResult(userId.toString(), email, nombre, apellido, telefono);
	}
}
