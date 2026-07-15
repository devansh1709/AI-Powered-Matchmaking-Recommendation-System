package com.MatchmakingBackend.repo;

import com.MatchmakingBackend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findByEmailIgnoreCase(String email);
	boolean existsByEmailIgnoreCase(String email);
}
