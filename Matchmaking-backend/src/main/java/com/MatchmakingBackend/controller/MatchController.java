package com.MatchmakingBackend.controller;

import com.MatchmakingBackend.security.CustomUserDetails;
import com.MatchmakingBackend.dto.MatchCandidate;
import com.MatchmakingBackend.service.MatchReportService;
import com.MatchmakingBackend.service.SemanticMatchService;
import com.MatchmakingBackend.entity.Profile;
import com.MatchmakingBackend.repo.ProfileRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

	private final MatchReportService matchReportService;
	private final SemanticMatchService semanticMatchService;
	private final ProfileRepository profileRepository;

	public MatchController(
			MatchReportService matchReportService,
			SemanticMatchService semanticMatchService,
			ProfileRepository profileRepository) {

		this.matchReportService = matchReportService;
		this.semanticMatchService = semanticMatchService;
		this.profileRepository = profileRepository;
	}

	@GetMapping
	public List<MatchCandidate> getMatches(
			@AuthenticationPrincipal CustomUserDetails user) {

		Long profileId = user.getProfileId();

		Profile profile = profileRepository.findById(profileId)
				.orElseThrow(() ->
						new ResponseStatusException(
								NOT_FOUND,
								"Profile not found"
						)
				);

		List<Profile> semanticCandidates =
				semanticMatchService.findSemanticCandidates(profile);

		if (semanticCandidates.isEmpty()) {
			return matchReportService.getMatchesForProfile(profileId);
		}

		return matchReportService.getMatchesForProfile(
				profileId,
				semanticCandidates
		);
	}
}