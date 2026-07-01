package com.MatchmakingBackend.ai;

public interface AiTextClient {
	boolean isConfigured();
	String model();
	String provider();
	String createTextResponse(String instructions, String input);
}
