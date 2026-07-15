package com.MatchmakingBackend.service;

import com.MatchmakingBackend.entity.Profile;
import com.MatchmakingBackend.repo.ProfileRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SemanticMatchService {

    private static final int TOP_K = 10;
    private static final double SIMILARITY_THRESHOLD = 0.5;

    private final VectorStore vectorStore;
    private final ProfileRepository profileRepository;

    public SemanticMatchService(VectorStore vectorStore, ProfileRepository profileRepository) {
        this.vectorStore = vectorStore;
        this.profileRepository = profileRepository;
    }

    public List<Profile> findSemanticCandidates(Profile forProfile) {
        String oppositeGender = "MALE".equalsIgnoreCase(forProfile.getGender()) ? "FEMALE" : "MALE";

        FilterExpressionBuilder b = new FilterExpressionBuilder();
        Filter.Expression filterExpression = b.eq("gender", oppositeGender).build();

        String queryText = "Looking for a partner: " + safe(forProfile.getPartnerExpectations())
                + ". About me: " + safe(forProfile.getAbout())
                + ". Life goals: " + safe(forProfile.getLifeGoals());

        SearchRequest searchRequest = SearchRequest.builder()
                .query(queryText)
                .topK(TOP_K)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .filterExpression(filterExpression)
                .build();

        return vectorStore.similaritySearch(searchRequest).stream()
                .map(document -> Long.parseLong(document.getMetadata().get("profileId").toString()))
                .filter(id -> !id.equals(forProfile.getId()))
                .map(profileRepository::findById)
                .flatMap(Optional::stream)
                .toList();
    }

    public List<Document> findComparableContext(Profile profileOne,
                                                Profile profileTwo) {

        String query =
                """
                Compare these two profiles.
    
                Profile One:
                %s
    
                Profile Two:
                %s
                """
                        .formatted(
                                buildProfileSummary(profileOne),
                                buildProfileSummary(profileTwo));

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(5)
                .similarityThreshold(0.5)
                .build();

        return vectorStore.similaritySearch(request);
    }

    private static String safe(String value) {
        return value == null || value.isBlank() ? "no specific preference stated" : value;
    }

    private String buildProfileSummary(Profile profile) {
        return """
            Name: %s
            Age: %d
            Gender: %s
            City: %s
            State: %s
            Religion: %s
            Community: %s
            Mother Tongue: %s
            Education: %s
            Profession: %s
            Annual Income: %s
            About: %s
            Life Goals: %s
            Partner Expectations: %s
            """
                .formatted(
                        safe(profile.getFullName()),
                        profile.getAge(),
                        safe(profile.getGender()),
                        safe(profile.getCity()),
                        safe(profile.getState()),
                        safe(profile.getReligion()),
                        safe(profile.getCommunity()),
                        safe(profile.getMotherTongue()),
                        safe(profile.getEducation()),
                        safe(profile.getProfession()),
                        safe(profile.getAnnualIncome()),
                        safe(profile.getAbout()),
                        safe(profile.getLifeGoals()),
                        safe(profile.getPartnerExpectations())
                );
    }
}