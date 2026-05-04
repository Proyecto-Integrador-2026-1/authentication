package com.redpatitas.authentication.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record BatchContactRequest(
    @NotEmpty List<UUID> userIds
) {}