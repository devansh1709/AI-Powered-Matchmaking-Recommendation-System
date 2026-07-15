package com.MatchmakingBackend.dto;

import com.MatchmakingBackend.entity.Profile;

import java.util.List;

public record MatchReport(
		Profile profileOne,
		Profile profileTwo,
		int overallScore,
		String confidence,
		String summary,
		List<CompatibilityScore> statistics,
		List<String> strengths,
		List<String> concerns,
		List<String> questionsToAsk,
		String recommendation,
		String aiNarrative,
		String disclaimer
) {
}
