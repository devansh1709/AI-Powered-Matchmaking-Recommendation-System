package com.MatchmakingBackend.connection;

import jakarta.validation.constraints.NotNull;

public record RespondToInterestRequest(@NotNull Long profileId) {
}
