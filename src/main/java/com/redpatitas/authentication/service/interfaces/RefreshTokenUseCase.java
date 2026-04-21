package com.redpatitas.authentication.service.interfaces;

public interface RefreshTokenUseCase {

	LoginUseCase.LoginResult refresh(RefreshCommand command);

	record RefreshCommand(String rawRefreshToken) {
	}
}
