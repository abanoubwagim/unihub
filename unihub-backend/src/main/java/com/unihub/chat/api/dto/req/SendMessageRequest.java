package com.unihub.chat.api.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SendMessageRequest(

        @NotNull(message = "Receiver ID is required")
        UUID receiverId,

        @NotBlank(message = "Message content cannot be empty")
        @Size(max = 4000, message = "Message cannot exceed 4000 characters")
        String content
) {
}
