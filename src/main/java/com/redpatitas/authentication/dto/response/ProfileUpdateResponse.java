package com.redpatitas.authentication.dto.response;

import java.util.UUID;

public record ProfileUpdateResponse(
    UUID id,
    String nombre,
    String apellido,
    String telefono,
    String email
) {}