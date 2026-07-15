package com.MatchmakingBackend.controller;

import com.MatchmakingBackend.security.CustomUserDetails;
import com.MatchmakingBackend.dto.ChatMessageResponse;
import com.MatchmakingBackend.service.ConnectionService;
import com.MatchmakingBackend.dto.ConversationResponse;
import com.MatchmakingBackend.dto.SendChatMessageRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

	private final ConnectionService connectionService;

	public ConversationController(ConnectionService connectionService) {
		this.connectionService = connectionService;
	}

	@GetMapping
	public List<ConversationResponse> getConversations(
			@AuthenticationPrincipal CustomUserDetails user
	) {
		return connectionService.getConversations(
				user.getProfileId()
		);
	}

	@GetMapping("/{conversationId}/messages")
	public List<ChatMessageResponse> getMessages(
			@PathVariable Long conversationId,
			@AuthenticationPrincipal CustomUserDetails user
	) {
		return connectionService.getMessages(
				conversationId,
				user.getProfileId()
		);
	}

	@PostMapping("/{conversationId}/messages")
	public ChatMessageResponse sendMessage(
			@PathVariable Long conversationId,
			@AuthenticationPrincipal CustomUserDetails user,
			@Valid @RequestBody SendChatMessageRequest command
	) {
		return connectionService.sendMessage(
				conversationId,
				user.getProfileId(),
				command
		);
	}
}