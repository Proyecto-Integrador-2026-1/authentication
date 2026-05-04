package com.redpatitas.authentication.dto.response;

import java.util.Map;
import java.util.UUID;

public record BatchContactResponse(
    Map<UUID, ContactInfoResponse> users
) {}