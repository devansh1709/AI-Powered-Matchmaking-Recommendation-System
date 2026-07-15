package com.MatchmakingBackend.controller;

import com.MatchmakingBackend.security.CustomUserDetails;
import com.MatchmakingBackend.service.ConnectionService;
import com.MatchmakingBackend.dto.ConversationResponse;
import com.MatchmakingBackend.dto.CreateInterestRequest;
import com.MatchmakingBackend.dto.InterestRequestResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interest-requests")
public class InterestRequestController {

	private final ConnectionService connectionService;

	public InterestRequestController(ConnectionService connectionService) {
		this.connectionService = connectionService;
	}

	@GetMapping
	public List<InterestRequestResponse> getRequests(
			@AuthenticationPrincipal CustomUserDetails user,
			@RequestParam(defaultValue = "incoming") String box
	) {
		return connectionService.getRequests(user.getProfileId(), box);
	}

	@PostMapping
	public InterestRequestResponse sendRequest(
			@AuthenticationPrincipal CustomUserDetails user,
			@Valid @RequestBody CreateInterestRequest command
	) {
		return connectionService.sendRequest(
				user.getProfileId(),
				command
		);
	}

	@PostMapping("/{requestId}/accept")
	public ConversationResponse acceptRequest(
			@PathVariable Long requestId,
			@AuthenticationPrincipal CustomUserDetails user
	) {
		return connectionService.acceptRequest(
				requestId,
				user.getProfileId()
		);
	}

	@PostMapping("/{requestId}/decline")
	public InterestRequestResponse declineRequest(
			@PathVariable Long requestId,
			@AuthenticationPrincipal CustomUserDetails user
	) {
		return connectionService.declineRequest(
				requestId,
				user.getProfileId()
		);
	}
}