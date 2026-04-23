package com.redpatitas.authentication.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para registrar un usuario")
public record RegisterUserRequest(
		@NotBlank @Size(max = 255) @Schema(example = "Juan") String nombre,
		@NotBlank @Size(max = 255) @Schema(example = "Perez") String apellido,
		@NotBlank
		@Pattern(regexp = "^\\d{7,15}$", message = "El teléfono debe contener solo números (7-15 dígitos)")
		@Schema(example = "3001112233") String telefono,
		@NotBlank @Email @Schema(example = "juan.perez@correo.com") String email,
		@NotBlank
		@Size(min = 8, max = 128)
		@Pattern(regexp = "^[\\S]{8,128}$", message = "La contraseña no puede contener espacios en blanco")
		@Schema(description = "Contraseña (8-128 caracteres, sin espacios)", example = "password123") String password) {
}
