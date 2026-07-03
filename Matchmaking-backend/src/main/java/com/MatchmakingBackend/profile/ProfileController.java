package com.MatchmakingBackend.profile;

import com.MatchmakingBackend.match.ProfileEmbeddingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
	private final ProfileRepository profileRepository;
	private final ProfileEmbeddingService profileEmbeddingService;

	public ProfileController(ProfileRepository profileRepository,
							 ProfileEmbeddingService profileEmbeddingService) { // add param
		this.profileRepository = profileRepository;
		this.profileEmbeddingService = profileEmbeddingService;
	}

	@GetMapping
	public List<Profile> searchProfiles(
			@RequestParam(required = false) String city,
			@RequestParam(required = false) String religion,
			@RequestParam(required = false) String gender
	) {
		return profileRepository.search(blankToNull(city), blankToNull(religion), blankToNull(gender));
	}

	@GetMapping("/{id}")
	public Profile getProfile(@PathVariable Long id) {
		return profileRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Profile not found"));
	}

	@PostMapping
	public Profile createProfile(@Valid @RequestBody Profile profile) {
		profile.setId(null);
		Profile saved = profileRepository.save(profile);
		profileEmbeddingService.indexProfile(saved); // add this line
		return saved;
	}

	@PostMapping("/reindex")
	public String reindex() {
		return profileEmbeddingService.reindexAll() + " profiles reindexed into Qdrant";
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
