package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmClientPort;
import nl.zerofifty.springaiaccelerator.infrastructure.queryexpansion.QueryExpansionAdvisor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapter that uses an LLM to expand a single user query into multiple alternative versions.
 * 
 * Tip: Since query expansion is a relatively simple task, it is often beneficial to use 
 * a cheaper and faster model for this adapter to reduce latency and costs.
 */
@Component
@Profile("query-expansion")
public class ExpansionQueryAdapter implements LlmClientPort {

    private static final Logger log = LoggerFactory.getLogger(ExpansionQueryAdapter.class);
    private final ChatClient chatClient;

    public ExpansionQueryAdapter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    private final String expansionPrompt = """
            You are an AI language model assistant. Your task is to generate maximum five
            different search queries to retrieve relevant documents from a vector database.
            
            IMPORTANT: If the user message contains multiple distinct questions or topics,\s
            break them down into individual, specific queries.\s
            
            Goal: Help the user overcome limitations of distance-based similarity search by:
            1. Decomposing complex questions into simpler sub-queries.
            2. Using different terminology for the same concepts.
            3. Focusing on different aspects of the original prompt.
            
            Provide these alternative questions separated by newlines.
            Original prompt: {prompt}
            """;

    @Override
    public Flux<String> call(String prompt) {
        try {
            String response = chatClient.prompt()
                    .user(u -> u.text(expansionPrompt).param("prompt", prompt))
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                return Flux.empty();
            }

            List<String> queries = Arrays.stream(response.split("\n"))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();

            return Flux.fromIterable(queries);
        } catch (Exception e) {
            log.error("Failed to expand query", e);
            return Flux.empty();
        }
    }
}
