package com.MatchmakingBackend.service;

import com.MatchmakingBackend.dto.ChatMessageResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageBroadcaster {

	private final SimpMessagingTemplate messagingTemplate;

	public ChatMessageBroadcaster(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	public void broadcast(ChatMessageResponse message) {
		messagingTemplate.convertAndSend(
				"/topic/conversations/" + message.conversationId(),
				message
		);
	}
}