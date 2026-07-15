package com.MatchmakingBackend.config;

import com.MatchmakingBackend.service.ProfileEmbeddingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class ProfileEmbeddingSeeder implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileEmbeddingSeeder.class);

    private final ProfileEmbeddingService profileEmbeddingService;

    public ProfileEmbeddingSeeder(ProfileEmbeddingService profileEmbeddingService) {
        this.profileEmbeddingService = profileEmbeddingService;
    }

    @Override
    public void run(String... args) {
        int count = profileEmbeddingService.reindexAll();
        LOGGER.info("Startup reindex: {} profiles embedded into Qdrant", count);
    }
}