package com.MatchmakingBackend.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiClientRouter {

	private final String preferredProvider;

	private final OllamaClient ollamaClient;

	private final GeminiClient geminiClient;

	public AiClientRouter(
			@Value("${app.ai.provider}") String preferredProvider,
			OllamaClient ollamaClient,
			GeminiClient geminiClient
	) {
		this.preferredProvider = preferredProvider;
		this.ollamaClient = ollamaClient;
		this.geminiClient = geminiClient;
	}

	public AiTextClient preferredClient() {
		if ("ollama".equalsIgnoreCase(preferredProvider)) {
			return ollamaClient;
		}
		return geminiClient;
	}

	public AiTextClient secondaryClient() {
		return geminiClient;
	}

	public AiTextClient fallbackClient() {
		return ollamaClient;
	}
}