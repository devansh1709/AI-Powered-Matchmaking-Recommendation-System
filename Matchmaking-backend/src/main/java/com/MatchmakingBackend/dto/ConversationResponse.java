package com.MatchmakingBackend.dto;

import com.MatchmakingBackend.entity.Conversation;
import com.MatchmakingBackend.entity.Profile;

import java.time.OffsetDateTime;

public record ConversationResponse(
		Long id,
		Long interestRequestId,
		Profile profileOne,
		Profile profileTwo,
		OffsetDateTime createdAt
) {
	public static ConversationResponse from(Conversation conversation) {
		return new ConversationResponse(
				conversation.getId(),
				conversation.getInterestRequest().getId(),
				conversation.getProfileOne(),
				conversation.getProfileTwo(),
				conversation.getCreatedAt()
		);
	}
}
