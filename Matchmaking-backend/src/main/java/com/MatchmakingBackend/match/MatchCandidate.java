package com.MatchmakingBackend.match;

import com.MatchmakingBackend.profile.Profile;

import java.util.List;

public record MatchCandidate(
		Profile profile,
		int overallScore,
		String confidence,
		List<String> topReasons,
		String recommendation
) {
}
