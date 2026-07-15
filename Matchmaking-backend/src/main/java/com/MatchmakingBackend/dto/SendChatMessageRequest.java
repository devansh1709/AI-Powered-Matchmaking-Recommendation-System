package com.MatchmakingBackend.dto;

import jakarta.validation.constraints.NotBlank;

public record SendChatMessageRequest(
		@NotBlank String message
) {
}
