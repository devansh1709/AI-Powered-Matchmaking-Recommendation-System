package com.MatchmakingBackend.auth;

import com.MatchmakingBackend.profile.Profile;

public record AuthResponse(
		String token,
		Profile profile
) {
}
