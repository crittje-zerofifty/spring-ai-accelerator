package nl.zerofifty.springaiaccelerator.infrastructure.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.ReactiveHealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;


@Component
@Profile("ollama")
public class OllamaHealthIndicator implements ReactiveHealthIndicator {

    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String baseUrl;

    private final WebClient webClient = WebClient.builder().build();
    private final AtomicInteger ollamaStatus = new AtomicInteger(0);

    public OllamaHealthIndicator(MeterRegistry registry) {
        registry.gauge("ollama.up", ollamaStatus);
    }

    @Override
    public Mono<Health> health() {
        return webClient
                .get()
                .uri(baseUrl + "/api/tags")
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    ollamaStatus.set(1);
                    return Health.up().withDetail("ollama", "Ollama is running").build();
                })
                .onErrorResume(ex -> {
                    ollamaStatus.set(0);
                    return Mono.just(Health.down().withDetail("ollama", "Ollama is unreachable").build());
                });
    }
}
