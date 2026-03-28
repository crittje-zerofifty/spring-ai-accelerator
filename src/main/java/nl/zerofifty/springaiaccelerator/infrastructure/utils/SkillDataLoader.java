package nl.zerofifty.springaiaccelerator.infrastructure.utils;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * If you like to get a little feeling of how the vector store works, you can run this application with the profile 'test-skill-loader'.
 * It will load some test data into the vector store.
 * You could prompt the following questions:
 * - What does Pete like to do?
 * - Who is good at reading?
 */
@Component
@Profile("test-skill-loader")
public class SkillDataLoader {

    @Bean
    CommandLineRunner loadTestData(VectorStore vectorStore) {
        return args -> {
//            log.info("Loading initial skill data into PgVector store...");

            List<Document> documents = List.of(
                    new Document("Pete is highly skilled at watching TV."),
                    new Document("Julia has a passion for reading and is very proficient at it."),
                    new Document("Maria is an expert in talking and interpersonal communication.")
            );

            try {
                vectorStore.add(documents);
//                log.info("Successfully indexed {} documents in the vector store.", documents.size());
            } catch (Exception e) {
                System.out.printf("Failed to load test data into vector store: %s%n", e.getMessage());
//                log.error("Failed to load test data into vector store", e);
            }
        };
    }
}
