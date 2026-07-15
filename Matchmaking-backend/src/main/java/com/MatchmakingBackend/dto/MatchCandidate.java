package com.MatchmakingBackend.dto;

import com.MatchmakingBackend.entity.Profile;

import java.util.List;

public record MatchCandidate(
		Profile profile,
		int overallScore,
		String confidence,
		List<String> topReasons,
		String recommendation
) {
}
