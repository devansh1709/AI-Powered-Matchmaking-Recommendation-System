package com.MatchmakingBackend.auth;

import com.MatchmakingBackend.profile.Profile;
import com.MatchmakingBackend.profile.ProfileRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AccountRepository accountRepository;
	private final ProfileRepository profileRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthController(AccountRepository accountRepository, ProfileRepository profileRepository,
						  PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.accountRepository = accountRepository;
		this.profileRepository = profileRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	@PostMapping("/signup")
	public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
		if (accountRepository.existsByEmailIgnoreCase(request.email())) {
			throw new ResponseStatusException(BAD_REQUEST, "Email is already registered");
		}

		Profile profile = request.profile();
		profile.setId(null);
		Profile savedProfile = profileRepository.save(profile);

		Account account = new Account();
		account.setEmail(request.email().trim().toLowerCase());
		account.setPhone(request.phone().trim());
		account.setPasswordHash(passwordEncoder.encode(request.password()));
		account.setProfile(savedProfile);
		account.setRole(Role.USER);
		accountRepository.save(account);

		return authResponse(account, savedProfile);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		Account account = accountRepository.findByEmailIgnoreCase(request.email())
				.orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Invalid email or password"));
		if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
			throw new ResponseStatusException(UNAUTHORIZED, "Invalid email or password");
		}
		return authResponse(account, account.getProfile());
	}

	private AuthResponse authResponse(Account account, Profile profile) {
		return new AuthResponse(jwtService.generateToken(new CustomUserDetails(account)), profile);
	}
}