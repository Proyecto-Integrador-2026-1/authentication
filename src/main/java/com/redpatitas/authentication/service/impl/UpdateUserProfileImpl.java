package com.redpatitas.authentication.service.impl;

import com.redpatitas.authentication.dto.request.UpdateUserRequest;
import com.redpatitas.authentication.dto.response.ProfileUpdateResponse;
import com.redpatitas.authentication.entity.UserEntity;
import com.redpatitas.authentication.exception.AuthenticationDomainException;
import com.redpatitas.authentication.repository.interfaces.UserJpaRepository;
import com.redpatitas.authentication.service.interfaces.PasswordHashService;
import com.redpatitas.authentication.service.interfaces.UpdateUserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateUserProfileImpl implements UpdateUserProfile {

    private final UserJpaRepository userRepository;
    private final PasswordHashService passwordHashService;

    @Override
    @Transactional
    public ProfileUpdateResponse updateProfile(UUID userId, UpdateUserRequest request) {
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new AuthenticationDomainException("USER_NOT_FOUND", "Usuario no encontrado"));

        // Verificar contraseña actual para cambios sensibles
        boolean isSensitiveChange = request.email() != null || request.newPassword() != null;
        if (isSensitiveChange) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new AuthenticationDomainException("CURRENT_PASSWORD_REQUIRED", "Se requiere la contraseña actual para modificar email o contraseña");
            }
            if (!passwordHashService.matches(request.currentPassword(), user.getPasswordHash())) {
                throw new AuthenticationDomainException("INVALID_CURRENT_PASSWORD", "Contraseña actual incorrecta");
            }
        }

        // Actualizar campos simples
        if (request.nombre() != null) user.setNombre(request.nombre());
        if (request.apellido() != null) user.setApellido(request.apellido());
        if (request.telefono() != null) user.setTelefono(request.telefono());

                // Cambio de email (verificar unicidad)
        if (request.email() != null && !request.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(request.email())) {
                throw new AuthenticationDomainException("EMAIL_ALREADY_EXISTS", "El email ya está registrado por otro usuario");
            }
            user.setEmail(request.email());
        }

        // Cambio de contraseña con doble verificación
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (!request.newPassword().equals(request.confirmPassword())) {
                throw new AuthenticationDomainException("PASSWORD_MISMATCH", "La nueva contraseña y su confirmación no coinciden");
            }
            String hashedPassword = passwordHashService.encode(request.newPassword());
            user.setPasswordHash(hashedPassword);
        }

        UserEntity saved = userRepository.save(user);

        return new ProfileUpdateResponse(
            saved.getId(),
            saved.getNombre(),
            saved.getApellido(),
            saved.getTelefono(),
            saved.getEmail()
        );
    }
}