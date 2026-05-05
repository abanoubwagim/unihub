package com.unihub.identity.infrastructure.oauth;

import com.unihub.shared.exception.BadRequestException;

import java.util.Map;
public class OAuth2UserInfoFactory {

    private OAuth2UserInfoFactory() {
    }

    public static OAuth2UserInfo create(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google"    -> new GoogleOAuth2UserInfo(attributes);
            case "microsoft" -> new MicrosoftOAuth2UserInfo(attributes);
            default          -> throw new BadRequestException(
                    "OAuth2 provider not supported: " + registrationId);
        };
    }
}