package com.MatchmakingBackend.ai;

public record AiChatResponse(
		String reply,
		boolean generatedByOpenAi,
		String model
) {
}
