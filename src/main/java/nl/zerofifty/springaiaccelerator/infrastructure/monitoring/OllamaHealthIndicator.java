package nl.zerofifty.springaiaccelerator.infrastructure.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Component
@Profile("ollama")
public class OllamaHealthIndicator implements ReactiveHealthIndicator {

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    private static final Logger log = LoggerFactory.getLogger(OllamaHealthIndicator.class);

    private final WebClient webClient = WebClient.builder().build();

    @Override
    public Mono<Health> health() {
        return webClient
                .get()
                .uri(baseUrl + "/api/tags")
                .retrieve()
                .toBodilessEntity()
                .map(response -> Health.up().withDetail("ollama", "Ollama is running").build())
                .onErrorResume(ex -> Mono.just(Health.down().withDetail("ollama", "Ollama is unreachable").build()));
    }
}
