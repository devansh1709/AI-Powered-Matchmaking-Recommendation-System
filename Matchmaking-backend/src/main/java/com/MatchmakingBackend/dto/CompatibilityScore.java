package com.MatchmakingBackend.dto;

public record CompatibilityScore(
		String label,
		int score,
		String reason
) {
}
