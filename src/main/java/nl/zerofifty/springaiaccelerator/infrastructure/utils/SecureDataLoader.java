package nl.zerofifty.springaiaccelerator.infrastructure.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * If you like to get a little feeling of how the vector store works, you can run this application with the profile
 * 'test-secure-data-loader'.

 * You could prompt the following questions:
 * What will happen if I cross the speed limit? Both users with PUBLIC and CONFIDENTIAL level will be able to see a
 * response.
 * Or more confidentially: Ask where the codebase of x is. And only the people with CONFIDENTIAL level will be able to see the answer.
 *
 */
@Component
@Profile("test-secure-data-loader")
public class SecureDataLoader {

    private static final Logger log = LoggerFactory.getLogger(SecureDataLoader.class);

    @Bean
    CommandLineRunner loadTestData(VectorStore vectorStore) {
        return args -> {
            log.info("Loading initial secure rag data into PgVector store...");

            List<Document> documents = List.of(
                    new Document("The company parking policy is: first come, first served. However, if you are " +
                            "avoiding speed bumps and speed limits of the parking lot, you will not be server at all",
                            Map.of(
                                    "level", "PUBLIC")),
                    new Document("The secret project 'X' codebase is located at internal-git.local/top-secret.", Map.of("level", "CONFIDENTIAL"))
            );

            try {
                vectorStore.add(documents);
                log.info("Successfully indexed {} documents in the vector store.", documents.size());
            } catch (Exception e) {
                log.error("Failed to load test data into vector store", e);
            }
        };
    }
}
