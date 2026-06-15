package com.unihub.notifications.api.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdatePreferencesRequest(

        @NotEmpty
        @Valid
        List<PreferenceEntry> preferences
) {
}