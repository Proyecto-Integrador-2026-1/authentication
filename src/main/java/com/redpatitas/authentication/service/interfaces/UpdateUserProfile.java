package com.redpatitas.authentication.service.interfaces;

import com.redpatitas.authentication.dto.request.UpdateUserRequest;
import com.redpatitas.authentication.dto.response.ProfileUpdateResponse;
import java.util.UUID;

public interface UpdateUserProfile {
    ProfileUpdateResponse updateProfile(UUID userId, UpdateUserRequest request);
}
