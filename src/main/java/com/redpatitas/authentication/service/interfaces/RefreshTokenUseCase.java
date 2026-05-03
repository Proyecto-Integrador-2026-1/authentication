package com.redpatitas.authentication.service.interfaces;

public interface RefreshTokenUseCase {

	LoginUseCase.LoginResult refresh(RefreshCommand command);

	void revoke(String rawRefreshToken);

	record RefreshCommand(String rawRefreshToken) {
	}

	
}
