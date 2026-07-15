package com.MatchmakingBackend.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OllamaClient implements AiTextClient {
	private final String model;
	private final ChatClient chatClient;

	public OllamaClient(
			@Qualifier("ollamaChatClient") ChatClient chatClient,
			@Value("${spring.ai.ollama.chat.options.model}") String model
	) {
		this.chatClient = chatClient;
		this.model = model;
	}

	@Override
	public boolean isConfigured() {
		return true;
	}

	@Override
	public String model() {
		return model;
	}

	@Override
	public String provider() {
		return "ollama";
	}

	@Override
	public String createTextResponse(String instructions, String input) {

		return chatClient.prompt()
				.system(instructions)
				.user(input)
				.call()
				.content();
	}
}
