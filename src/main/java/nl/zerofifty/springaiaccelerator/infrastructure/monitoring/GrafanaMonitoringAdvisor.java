package nl.zerofifty.springaiaccelerator.infrastructure.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GrafanaMonitoringAdvisor implements StreamAdvisor {

    private final MeterRegistry registry;

    public GrafanaMonitoringAdvisor(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        long start = System.nanoTime();

        return chain.nextStream(request)
                .doOnNext(advisedResponse -> {
                    if (advisedResponse.chatResponse() != null) {
                        recordStats(advisedResponse.chatResponse(), request.context(), System.nanoTime() - start);
                    }
                });
    }

    private void recordStats(ChatResponse response, Map<String, Object> params, long durationNs) {
        if (response != null) {
            String model = response.getMetadata().getModel();
            String chatId = String.valueOf(params.getOrDefault("chat_memory_conversation_id", "unknown"));

            Timer.builder("ai.interaction.duration")
                    .tag("model", model)
                    .tag("chat_id", chatId)
                    .register(registry)
                    .record(durationNs, TimeUnit.NANOSECONDS);

            Counter.builder("ai.tokens.in")
                    .tag("model", model)
                    .tag("chat_id", chatId)
                    .register(registry)
                    .increment(response.getMetadata().getUsage().getPromptTokens());

            Counter.builder("ai.tokens.out")
                    .tag("model", model)
                    .tag("chat_id", chatId)
                    .register(registry)
                    .increment(response.getMetadata().getUsage().getCompletionTokens());

            Counter.builder("ai.tokens.total")
                    .tag("model", model)
                    .tag("chat_id", chatId)
                    .register(registry)
                    .increment(response.getMetadata().getUsage().getTotalTokens());
        }
    }

    @Override
    public String getName() {
        return "GrafanaMonitoringAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
