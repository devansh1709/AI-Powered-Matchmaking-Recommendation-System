package com.MatchmakingBackend.dto;

import jakarta.validation.constraints.NotNull;

public record RespondToInterestRequest(@NotNull Long profileId) {
}
