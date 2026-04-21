package com.redpatitas.authentication.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario creado correctamente")
public record RegisterUserResponseBody(
		@Schema(example = "33333333-3333-3333-3333-333333333333") String userId,
		@Schema(example = "juan.perez@correo.com") String email,
		@Schema(example = "Juan") String nombre,
		@Schema(example = "Perez") String apellido,
		@Schema(example = "+573001112233") String telefono) {
}
