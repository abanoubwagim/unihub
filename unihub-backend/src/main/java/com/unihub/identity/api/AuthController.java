package com.unihub.identity.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.unihub.identity.application.RegisterUserUseCase;

import com.unihub.identity.api.dto.LoginRequest;
import com.unihub.identity.api.dto.LoginResponse;
import com.unihub.identity.api.dto.RegisterReqeust;
import com.unihub.identity.api.dto.RegisterResponse;
import com.unihub.identity.application.LoginUserUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final LoginUserUseCase loginUserUseCase;
    private final RegisterUserUseCase registerUserUseCase;

    @PostMapping("/login")
    public LoginResponse login(
        @Valid @RequestBody LoginRequest request
    ){
        return loginUserUseCase.login(request);
    }


    @PostMapping("/register")
    public RegisterResponse register(
        @Valid @RequestBody RegisterReqeust request
    ){
        return registerUserUseCase.register(request);
    }

    


}
