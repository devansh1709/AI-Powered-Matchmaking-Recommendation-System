package com.MatchmakingBackend.match;

import com.MatchmakingBackend.profile.Profile;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ProfileDocumentMapper {

    public Document toDocument(Profile profile) {
        String content = buildSemanticText(profile);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("profileId", profile.getId());
        metadata.put("gender", normalize(profile.getGender()));
        metadata.put("city", normalize(profile.getCity()));
        metadata.put("religion", normalize(profile.getReligion()));

        String documentId = UUID.nameUUIDFromBytes(
                ("profile:" + profile.getId()).getBytes(StandardCharsets.UTF_8)
        ).toString();

        return new Document(documentId, content, metadata);
    }
    private String buildSemanticText(Profile profile) {
        return """
                %s, %d years old, %s, based in %s, %s.
                Education: %s. Profession: %s.
                Diet: %s, smoking: %s, drinking: %s.
                Family type: %s. Wants children: %s.
                About: %s
                Interests: %s
                Life goals: %s
                Looking for a partner who: %s
                """.formatted(
                safe(profile.getFullName()),
                profile.getAge(),
                safe(profile.getGender()),
                safe(profile.getCity()),
                safe(profile.getState()),
                safe(profile.getEducation()),
                safe(profile.getProfession()),
                safe(profile.getDiet()),
                safe(profile.getSmoking()),
                safe(profile.getDrinking()),
                safe(profile.getFamilyType()),
                safe(profile.getWantsChildren()),
                safe(profile.getAbout()),
                safe(profile.getInterests()),
                safe(profile.getLifeGoals()),
                safe(profile.getPartnerExpectations())
        );
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "not specified" : value;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value.trim().toUpperCase();
    }
}
