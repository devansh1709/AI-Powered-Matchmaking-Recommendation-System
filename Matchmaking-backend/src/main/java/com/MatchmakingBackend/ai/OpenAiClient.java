package com.MatchmakingBackend.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAiClient implements AiTextClient {

	private final ChatClient chatClient;
	private final String model;

	public OpenAiClient(
			@Qualifier("openAiChatClient") ChatClient chatClient,
			@Value("${spring.ai.openai.chat.options.model}") String model
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
		return "openai";
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