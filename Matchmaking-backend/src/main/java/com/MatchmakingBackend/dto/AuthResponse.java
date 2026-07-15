package com.MatchmakingBackend.dto;

import com.MatchmakingBackend.entity.Profile;

public record AuthResponse(
		String token,
		Profile profile
) {
}
