package com.unihub.identity.api.dto;

public record LoginResponse(

    String accessToken,
    String tokenType
) {

}
