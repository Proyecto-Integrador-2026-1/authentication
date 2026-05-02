package com.redpatitas.authentication.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    @Size(min = 2, max = 100) String nombre,
    @Size(min = 2, max = 100) String apellido,
    @Size(max = 20) String telefono,
    
    @Email
    @Size(max = 255)
    String email,  // nuevo email (opcional, pero requiere contraseña actual si se cambia)
    
    @Size(min = 6, max = 100)
    String newPassword,  // nueva contraseña (opcional)
    
    @Size(min = 6, max = 100)
    String confirmPassword, // confirmación de nueva contraseña
    
    @NotBlank(message = "La contraseña actual es obligatoria para cualquier cambio sensible")
    String currentPassword   // siempre requerida si se cambia email o password
) {}