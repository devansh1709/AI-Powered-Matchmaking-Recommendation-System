package com.MatchmakingBackend.service;

import com.MatchmakingBackend.entity.Profile;
import com.MatchmakingBackend.repo.ProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileEmbeddingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileEmbeddingService.class);

    private final VectorStore vectorStore;
    private final ProfileDocumentMapper profileDocumentMapper;
    private final ProfileRepository profileRepository;

    public ProfileEmbeddingService(VectorStore vectorStore,
                                   ProfileDocumentMapper profileDocumentMapper,
                                   ProfileRepository profileRepository) {
        this.vectorStore = vectorStore;
        this.profileDocumentMapper = profileDocumentMapper;
        this.profileRepository = profileRepository;
    }

    public void indexProfile(Profile profile) {
        vectorStore.add(List.of(profileDocumentMapper.toDocument(profile)));
        LOGGER.info("Indexed profile {} into Qdrant", profile.getId());
    }

    public void deleteProfile(Long profileId) {
        vectorStore.delete(
                new FilterExpressionBuilder()
                        .eq("profileId", profileId)
                        .build()
        );
    }
    public int reindexAll() {
        List<Document> documents = profileRepository.findAll().stream()
                .map(profileDocumentMapper::toDocument)
                .toList();
        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
        LOGGER.info("Reindexed {} profiles into Qdrant", documents.size());
        return documents.size();
    }
}