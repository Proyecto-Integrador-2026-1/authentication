package com.redpatitas.authentication.service.interfaces;

public interface RegisterUserUseCase {

	RegisterResult register(RegisterCommand command);

	record RegisterCommand(String nombre, String apellido, String telefono, String email, String rawPassword) {
	}

	record RegisterResult(String userId, String email, String nombre, String apellido, String telefono) {
	}
}
