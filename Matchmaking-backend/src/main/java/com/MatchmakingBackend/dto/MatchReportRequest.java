package com.MatchmakingBackend.dto;

import jakarta.validation.constraints.NotNull;

public record MatchReportRequest(
		@NotNull Long profileOneId,
		@NotNull Long profileTwoId
) {
}
