package com.unihub.notifications.api.dto.req;

import com.unihub.notifications.domain.enums.NotificationType;
import jakarta.validation.constraints.NotNull;

public record PreferenceEntry(

        @NotNull
        NotificationType notificationType,

        boolean inAppEnabled
) {
}
