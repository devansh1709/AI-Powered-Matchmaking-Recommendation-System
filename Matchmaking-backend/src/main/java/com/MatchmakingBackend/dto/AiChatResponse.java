package com.MatchmakingBackend.dto;

public record AiChatResponse(
		String reply,
		boolean generatedByOpenAi,
		String model
) {
}
