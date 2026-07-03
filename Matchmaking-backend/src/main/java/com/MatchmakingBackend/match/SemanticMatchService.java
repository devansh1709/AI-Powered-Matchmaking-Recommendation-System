package com.MatchmakingBackend.match;

import com.MatchmakingBackend.profile.Profile;
import com.MatchmakingBackend.profile.ProfileRepository;
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

    private static String safe(String value) {
        return value == null || value.isBlank() ? "no specific preference stated" : value;
    }
}