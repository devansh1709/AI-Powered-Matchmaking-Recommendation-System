package com.MatchmakingBackend.config;

import com.MatchmakingBackend.entity.Account;
import com.MatchmakingBackend.repo.AccountRepository;
import com.MatchmakingBackend.entity.Profile;
import com.MatchmakingBackend.repo.ProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Order(2)
public class AccountSeeder implements CommandLineRunner {
	private final AccountRepository accountRepository;
	private final ProfileRepository profileRepository;
	private final PasswordEncoder passwordEncoder;

	public AccountSeeder(AccountRepository accountRepository, ProfileRepository profileRepository,
						 PasswordEncoder passwordEncoder) {
		this.accountRepository = accountRepository;
		this.profileRepository = profileRepository;
		this.passwordEncoder = passwordEncoder;
	}


	@Override
	public void run(String... args) {
		Map<String, String> seedEmails = new LinkedHashMap<>();
		seedEmails.put("Aarav Mehta", "aarav@example.com");
		seedEmails.put("Ananya Sharma", "ananya@example.com");
		seedEmails.put("Kabir Khan", "kabir@example.com");
		seedEmails.put("Meera Iyer", "meera@example.com");
		seedEmails.put("Rohan Singh", "rohan@example.com");
		seedEmails.put("Priya Nair", "priya@example.com");
		seedEmails.put("Arjun Malhotra", "arjun@example.com");
		seedEmails.put("Dev Patel", "dev@example.com");
		seedEmails.put("Ishaan Rao", "ishaan@example.com");
		seedEmails.put("Neel Bhatia", "neel@example.com");
		seedEmails.put("Samar Verma", "samar@example.com");
		seedEmails.put("Tara Kapoor", "tara@example.com");
		seedEmails.put("Nisha Menon", "nisha@example.com");

		seedEmails.forEach((fullName, email) ->
				profileRepository.findByFullName(fullName).ifPresent(profile -> createAccount(email, profile)));
	}

	private void createAccount(String email, Profile profile) {
		if (accountRepository.existsByEmailIgnoreCase(email)) {
			return;
		}

		Account account = new Account();
		account.setEmail(email);
		account.setPhone("+91-90000-00000");
		account.setPasswordHash(passwordEncoder.encode("Password@123"));
		account.setProfile(profile);
		accountRepository.save(account);
	}
}
