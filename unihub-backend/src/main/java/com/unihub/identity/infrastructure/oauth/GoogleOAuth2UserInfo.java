package com.unihub.identity.infrastructure.oauth;

import java.util.Map;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        String email = (String) attributes.get("email");
        if (email == null || email.isBlank()) {
            email = (String) attributes.get("preferred_username");
        }
        return email;
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}