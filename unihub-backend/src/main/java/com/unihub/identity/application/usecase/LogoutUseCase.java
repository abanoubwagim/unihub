package com.unihub.identity.application.usecase;

public interface LogoutUseCase {

    void logout(String rawAccessToken);
    
    void logout(String rawAccessToken, String rawRefreshToken);


}