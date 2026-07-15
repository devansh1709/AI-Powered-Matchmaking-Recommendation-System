package com.MatchmakingBackend.dto;

import jakarta.validation.constraints.NotNull;

public record CreateInterestRequest(
		@NotNull Long receiverProfileId
) {
}