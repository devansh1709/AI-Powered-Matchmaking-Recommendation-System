package com.MatchmakingBackend.controller;

import com.MatchmakingBackend.dto.LoginRequest;
import com.MatchmakingBackend.dto.SignupRequest;
import com.MatchmakingBackend.enums.Role;
import com.MatchmakingBackend.entity.Account;
import com.MatchmakingBackend.repo.AccountRepository;
import com.MatchmakingBackend.security.CustomUserDetails;
import com.MatchmakingBackend.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.MatchmakingBackend.entity.Profile;
import com.MatchmakingBackend.repo.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private AccountRepository accountRepository;
    private ProfileRepository profileRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        profileRepository = mock(ProfileRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);

        AuthController controller = new AuthController(accountRepository, profileRepository, passwordEncoder, jwtService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void signup_createsAccountAndReturnsToken() throws Exception {
        Profile profile = new Profile();
        profile.setFullName("Test User");
        profile.setGender("MALE");
        profile.setAge(28);

        SignupRequest request = new SignupRequest("test@example.com", "+91-90000-00000", "Password@123", profile);

        when(accountRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
            Profile saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(passwordEncoder.encode("Password@123")).thenReturn("hashed-password");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("signed-jwt-token");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("signed-jwt-token"))
                .andExpect(jsonPath("$.profile.fullName").value("Test User"));

        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void signup_rejectsDuplicateEmail() throws Exception {
        Profile profile = new Profile();
        profile.setFullName("Test User");
        profile.setGender("MALE");
        profile.setAge(28);

        SignupRequest request = new SignupRequest("existing@example.com", "+91-90000-00000", "Password@123", profile);

        when(accountRepository.existsByEmailIgnoreCase("existing@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(accountRepository, never()).save(any());
    }

    @Test
    void login_returnsTokenForValidCredentials() throws Exception {
        Account account = new Account();
        account.setEmail("test@example.com");
        account.setPasswordHash("hashed-password");
        account.setRole(Role.USER);
        Profile profile = new Profile();
        profile.setId(1L);
        profile.setFullName("Test User");
        account.setProfile(profile);

        LoginRequest request = new LoginRequest("test@example.com", "Password@123");

        when(accountRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("Password@123", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("signed-jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("signed-jwt-token"));
    }

    @Test
    void login_rejectsWrongPassword() throws Exception {
        Account account = new Account();
        account.setEmail("test@example.com");
        account.setPasswordHash("hashed-password");
        account.setRole(Role.USER);

        LoginRequest request = new LoginRequest("test@example.com", "WrongPassword");

        when(accountRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("WrongPassword", "hashed-password")).thenReturn(false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
