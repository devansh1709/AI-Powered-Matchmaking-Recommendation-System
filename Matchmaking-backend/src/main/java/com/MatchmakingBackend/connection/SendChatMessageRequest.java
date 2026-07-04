package com.MatchmakingBackend.connection;

import jakarta.validation.constraints.NotBlank;

public record SendChatMessageRequest(
		@NotBlank String message
) {
}
