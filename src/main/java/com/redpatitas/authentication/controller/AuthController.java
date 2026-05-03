package com.redpatitas.authentication.controller;

import java.util.List;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.redpatitas.authentication.service.interfaces.LoginUseCase;
import com.redpatitas.authentication.service.interfaces.LoginUseCase.LoginCommand;
import com.redpatitas.authentication.service.interfaces.LoginUseCase.LoginResult;
import com.redpatitas.authentication.service.interfaces.RegisterUserUseCase;
import com.redpatitas.authentication.service.interfaces.RegisterUserUseCase.RegisterCommand;
import com.redpatitas.authentication.service.interfaces.RefreshTokenUseCase;
import com.redpatitas.authentication.service.interfaces.RefreshTokenUseCase.RefreshCommand;
import com.redpatitas.authentication.dto.request.LoginRequest;
import com.redpatitas.authentication.dto.request.LogoutRequest;
import com.redpatitas.authentication.dto.request.RegisterUserRequest;
import com.redpatitas.authentication.dto.response.LoginResponseBody;
import com.redpatitas.authentication.dto.response.MeResponseBody;
import com.redpatitas.authentication.dto.response.RegisterUserResponseBody;
import com.redpatitas.authentication.dto.request.RefreshRequest;
import com.redpatitas.authentication.config.security.JwtPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login con JWT (RBAC en claims)")
public class AuthController {

	private final LoginUseCase loginUseCase;
	private final RegisterUserUseCase registerUserUseCase;
	private final RefreshTokenUseCase refreshTokenUseCase;

	@Operation(summary = "Registro", description = "Crea un nuevo usuario y asigna rol por defecto.")
	@PostMapping(
			value = "/register",
			produces = { MediaType.APPLICATION_JSON_VALUE, "application/hal+json" },
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityModel<RegisterUserResponseBody>> register(
			@Valid @RequestBody RegisterUserRequest request,
			HttpServletRequest httpRequest) {
		var result = registerUserUseCase.register(
				new RegisterCommand(
						request.nombre(),
						request.apellido(),
						request.telefono(),
						request.email(),
						request.password()));

		var body = new RegisterUserResponseBody(
				result.userId(),
				result.email(),
				result.nombre(),
				result.apellido(),
				result.telefono());
		EntityModel<RegisterUserResponseBody> model = EntityModel.of(body);
		model.add(buildLink(httpRequest, "/api/v1/auth/login", "login"));
		model.add(buildLink(httpRequest, "/swagger-ui/index.html", "describedby"));
		return ResponseEntity.ok(model);
	}

	@Operation(summary = "Login", description = "Autentica por correo y contraseña; devuelve JWT Bearer.")
	@PostMapping(
			value = "/login",
			produces = { MediaType.APPLICATION_JSON_VALUE, "application/hal+json" },
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityModel<LoginResponseBody>> login(
			@Valid @RequestBody LoginRequest request,
			HttpServletRequest httpRequest) {
		var result = loginUseCase.login(new LoginCommand(request.email(), request.password()));
		return ResponseEntity.ok(toLoginModel(result, httpRequest));
	}

	@Operation(
			summary = "Renovar access token",
			description = "Envía el refresh token opaco; devuelve nuevos access + refresh (rotación).")
	@PostMapping(
			value = "/refresh",
			produces = { MediaType.APPLICATION_JSON_VALUE, "application/hal+json" },
			consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<EntityModel<LoginResponseBody>> refresh(
			@Valid @RequestBody RefreshRequest request,
			HttpServletRequest httpRequest) {
		var result = refreshTokenUseCase.refresh(new RefreshCommand(request.refreshToken()));
		return ResponseEntity.ok(toLoginModel(result, httpRequest));
	}

	private EntityModel<LoginResponseBody> toLoginModel(LoginResult result, HttpServletRequest request) {
		var body = new LoginResponseBody(
				result.accessToken(),
				result.tokenType(),
				result.expiresInSeconds(),
				result.roles(),
				result.refreshToken(),
				result.refreshExpiresInSeconds());
		EntityModel<LoginResponseBody> model = EntityModel.of(body);
		model.add(buildLink(request, "/swagger-ui/index.html", "describedby"));
		model.add(buildLink(request, "/v3/api-docs", "openapi"));
		model.add(buildLink(request, "/api/v1/auth/refresh", "refresh"));
		return model;
	}

	@Operation(
			summary = "Perfil actual (JWT)",
			description = "Devuelve datos del token Bearer (RBAC).",
			security = @SecurityRequirement(name = "bearer-jwt"))
	@GetMapping(value = "/me", produces = { MediaType.APPLICATION_JSON_VALUE, "application/hal+json" })
	public ResponseEntity<EntityModel<MeResponseBody>> me(
			Authentication authentication,
			HttpServletRequest request) {
		if (authentication == null || !(authentication.getPrincipal() instanceof JwtPrincipal p)) {
			return ResponseEntity.status(401).build();
		}
		List<String> roles = authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.toList();
		var body = new MeResponseBody(p.userId(), p.email(), roles);
		EntityModel<MeResponseBody> model = EntityModel.of(body);
		model.add(buildLink(request, "/api/v1/auth/me", "self"));
		model.add(buildLink(request, "/swagger-ui/index.html", "describedby"));
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

	@Operation(summary = "Logout", description = "Revoca el refresh token activo")
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
		refreshTokenUseCase.revoke(request.refreshToken());
		return ResponseEntity.noContent().build();
	}
}
