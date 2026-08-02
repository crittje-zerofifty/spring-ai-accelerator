package nl.zerofifty.springaiaccelerator.infrastructure.config;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmClientPort;
import nl.zerofifty.springaiaccelerator.infrastructure.queryexpansion.QueryExpansionAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("query-expansion")
public class QueryExpansionConfig {

    @Bean
    public QueryExpansionAdvisor queryExpansionAdvisor(VectorStore vectorStore, LlmClientPort llmClientPort) {
        return new QueryExpansionAdvisor(vectorStore, llmClientPort);
    }
}
