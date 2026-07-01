package com.MatchmakingBackend.connection;

import jakarta.validation.constraints.NotNull;

public record CreateInterestRequest(
		@NotNull Long senderProfileId,
		@NotNull Long receiverProfileId
) {
}
