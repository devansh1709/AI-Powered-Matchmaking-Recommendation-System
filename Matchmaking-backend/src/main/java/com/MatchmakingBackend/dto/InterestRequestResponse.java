package com.MatchmakingBackend.dto;

import com.MatchmakingBackend.entity.InterestRequest;
import com.MatchmakingBackend.enums.InterestRequestStatus;
import com.MatchmakingBackend.entity.Profile;

import java.time.OffsetDateTime;

public record InterestRequestResponse(
		Long id,
		Profile senderProfile,
		Profile receiverProfile,
		InterestRequestStatus status,
		Long conversationId,
		OffsetDateTime createdAt,
		OffsetDateTime respondedAt
) {
	public static InterestRequestResponse from(InterestRequest request) {
		Long conversationId = request.getConversation() == null ? null : request.getConversation().getId();
		return new InterestRequestResponse(
				request.getId(),
				request.getSenderProfile(),
				request.getReceiverProfile(),
				request.getStatus(),
				conversationId,
				request.getCreatedAt(),
				request.getRespondedAt()
		);
	}
}
