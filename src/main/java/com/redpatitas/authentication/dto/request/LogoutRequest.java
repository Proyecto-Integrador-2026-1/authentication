package com.redpatitas.authentication.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest( @NotBlank String refreshToken) {}