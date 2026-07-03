package com.MatchmakingBackend.match;

import com.MatchmakingBackend.ai.AiAdvisorService;
import com.MatchmakingBackend.profile.Profile;
import com.MatchmakingBackend.profile.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MatchReportServiceTest {

    private ProfileRepository profileRepository;
    private MatchReportService matchReportService;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        AiAdvisorService aiAdvisorService = mock(AiAdvisorService.class);
        matchReportService = new MatchReportService(profileRepository, aiAdvisorService);
    }

    @Test
    void createCalculatedReport_scoresSameCityHigherThanDifferentCity() {
        Profile sameCityOne = profile(1L, "Aarav", "MALE", "Mumbai", "Maharashtra");
        Profile sameCityTwo = profile(2L, "Ananya", "FEMALE", "Mumbai", "Maharashtra");
        Profile differentCity = profile(3L, "Priya", "FEMALE", "Delhi", "Delhi");

        when(profileRepository.findById(1L)).thenReturn(Optional.of(sameCityOne));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(sameCityTwo));
        when(profileRepository.findById(3L)).thenReturn(Optional.of(differentCity));

        MatchReport sameCityReport = matchReportService.createCalculatedReport(1L, 2L);
        MatchReport differentCityReport = matchReportService.createCalculatedReport(1L, 3L);

        assertThat(sameCityReport.overallScore()).isGreaterThan(differentCityReport.overallScore());
    }

    @Test
    void createCalculatedReport_throwsWhenProfileMissing() {
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchReportService.createCalculatedReport(99L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Profile not found");
    }

    @Test
    void getMatchesForProfile_excludesSameGenderAndSelf() {
        Profile self = profile(1L, "Aarav", "MALE", "Mumbai", "Maharashtra");
        Profile sameGender = profile(2L, "Rohan", "MALE", "Mumbai", "Maharashtra");
        Profile oppositeGender = profile(3L, "Ananya", "FEMALE", "Mumbai", "Maharashtra");

        when(profileRepository.findById(1L)).thenReturn(Optional.of(self));
        when(profileRepository.findById(3L)).thenReturn(Optional.of(oppositeGender));
        when(profileRepository.findAll()).thenReturn(List.of(self, sameGender, oppositeGender));

        List<MatchCandidate> matches = matchReportService.getMatchesForProfile(1L);

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().profile().getId()).isEqualTo(3L);
    }

    @Test
    void getMatchesForProfile_withCandidateList_onlyScoresGivenCandidates() {
        Profile self = profile(1L, "Aarav", "MALE", "Mumbai", "Maharashtra");
        Profile candidate = profile(2L, "Ananya", "FEMALE", "Delhi", "Delhi");

        when(profileRepository.findById(1L)).thenReturn(Optional.of(self));
        when(profileRepository.findById(2L)).thenReturn(Optional.of(candidate));

        List<MatchCandidate> matches = matchReportService.getMatchesForProfile(1L, List.of(candidate));

        assertThat(matches).hasSize(1);
        assertThat(matches.getFirst().profile().getId()).isEqualTo(2L);
    }

    private static Profile profile(Long id, String name, String gender, String city, String state) {
        Profile profile = new Profile();
        profile.setId(id);
        profile.setFullName(name);
        profile.setGender(gender);
        profile.setAge(28);
        profile.setCity(city);
        profile.setState(state);
        profile.setDiet("Vegetarian");
        profile.setSmoking("Never");
        profile.setDrinking("Never");
        profile.setFamilyType("Nuclear");
        profile.setWantsChildren("Yes");
        profile.setRelocation("Open to relocation");
        profile.setReligion("Hindu");
        profile.setLifeGoals("Focused on career and family");
        profile.setPartnerExpectations("Looking for someone supportive of my career");
        return profile;
    }
}