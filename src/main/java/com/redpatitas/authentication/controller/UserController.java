package com.redpatitas.authentication.controller;

import com.redpatitas.authentication.dto.request.BatchContactRequest;
import com.redpatitas.authentication.dto.request.UpdateUserRequest;
import com.redpatitas.authentication.dto.response.BatchContactResponse;
import com.redpatitas.authentication.dto.response.ContactInfoResponse;
import com.redpatitas.authentication.dto.response.ProfileUpdateResponse;
import com.redpatitas.authentication.config.security.JwtPrincipal;
import com.redpatitas.authentication.entity.UserEntity;
import com.redpatitas.authentication.exception.AuthenticationDomainException;
import com.redpatitas.authentication.repository.interfaces.UserJpaRepository;
import com.redpatitas.authentication.service.interfaces.UpdateUserProfile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UpdateUserProfile updateUserProfileUseCase;
    private final UserJpaRepository userRepository;
    @Value("${auth.internal.api-key}")
    private String internalApiKey;


    // Obtener perfil completo del usuario autenticado
    @Operation(summary = "Perfil completo", security = @SecurityRequirement(name = "bearer-jwt"))
    @GetMapping(value = "/me", produces = { MediaType.APPLICATION_JSON_VALUE, "application/hal+json" })
    public ResponseEntity<EntityModel<ProfileUpdateResponse>> getMyProfile(
            @AuthenticationPrincipal JwtPrincipal principal,
            HttpServletRequest request) {
        UUID userId = UUID.fromString(principal.userId()); // asumiendo que userId() devuelve String
        UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new AuthenticationDomainException("USER_NOT_FOUND", "Usuario no encontrado"));
        ProfileUpdateResponse body = new ProfileUpdateResponse(
                user.getId(),
                user.getNombre(),
                user.getApellido(),
                user.getTelefono(),
                user.getEmail()
        );
        EntityModel<ProfileUpdateResponse> model = EntityModel.of(body);
        model.add(buildLink(request, "/api/v1/users/me", "self"));
        model.add(buildLink(request, "/api/v1/auth/me", "auth-self")); // enlace al endpoint básico
        model.add(buildLink(request, "/swagger-ui/index.html", "describedby"));
        return ResponseEntity.ok(model);
    }

    // Actualizar perfil (nombre, apellido, teléfono, email, contraseña)
    @Operation(summary = "Actualizar perfil", security = @SecurityRequirement(name = "bearer-jwt"))
    @PutMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<ProfileUpdateResponse>> updateMyProfile(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody UpdateUserRequest request,
            HttpServletRequest httpRequest) {
        UUID userId = UUID.fromString(principal.userId());
        ProfileUpdateResponse updated = updateUserProfileUseCase.updateProfile(userId, request);
        EntityModel<ProfileUpdateResponse> model = EntityModel.of(updated);
        model.add(buildLink(httpRequest, "/api/v1/users/me", "self"));
        model.add(buildLink(httpRequest, "/api/v1/users/me", "updated"));
        model.add(buildLink(httpRequest, "/swagger-ui/index.html", "describedby"));
        return ResponseEntity.ok(model);
    }

    private static Link buildLink(HttpServletRequest request, String path, String rel) {
        String href = ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(path)
                .replaceQuery(null)
                .build()
                .toUriString();
        return Link.of(href, rel);
    }

    @GetMapping("/internal/{userId}/contact")
    public ResponseEntity<ContactInfoResponse> getContactInfoInternal(
            @PathVariable UUID userId,
            @RequestHeader("X-Internal-API-Key") String apiKey) {
        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthenticationDomainException("USER_NOT_FOUND", "Usuario no encontrado"));
        return ResponseEntity.ok(new ContactInfoResponse(
            user.getNombre(), user.getApellido()
        ));
    }

    @PostMapping("/internal/batch/contact")
    public ResponseEntity<BatchContactResponse> getBatchContactInfoInternal(
            @RequestBody BatchContactRequest request,
            @RequestHeader("X-Internal-API-Key") String apiKey) {
        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<UUID, ContactInfoResponse> result = new HashMap<>();
        for (UUID userId : request.userIds()) {
            userRepository.findById(userId).ifPresent(user -> 
                result.put(userId, new ContactInfoResponse(
                    user.getNombre(), user.getApellido()
                ))
            );
        }
        return ResponseEntity.ok(new BatchContactResponse(result));
    }
}