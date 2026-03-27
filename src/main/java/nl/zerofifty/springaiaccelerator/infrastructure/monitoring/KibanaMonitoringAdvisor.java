package nl.zerofifty.springaiaccelerator.infrastructure.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.kv;

public class KibanaMonitoringAdvisor implements StreamAdvisor {

    private static final Logger log = LoggerFactory.getLogger(KibanaMonitoringAdvisor.class);

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        long start = System.currentTimeMillis();

        return chain.nextStream(request)
                .doOnNext(advisedResponse -> {
                    if (advisedResponse.chatResponse() != null) {
                        logStats(advisedResponse.chatResponse(), request.context(), System.currentTimeMillis() - start);
                    }
                });
    }

    private void logStats(ChatResponse response, Map<String, Object> params, long duration) {
        if (response != null) {
            log.info("AI Interaction Monitored",
                    kv("chat_id", params.getOrDefault("chat_memory_conversation_id", "unknown")),
                    kv("tokens_in", response.getMetadata().getUsage().getPromptTokens()),
                    kv("tokens_out", response.getMetadata().getUsage().getCompletionTokens()),
                    kv("total_tokens", response.getMetadata().getUsage().getTotalTokens()),
                    kv("duration_ms", duration),
                    kv("model", response.getMetadata().getModel())
            );
        }
    }

    @Override
    public String getName() {
        return "KibanaMonitoringAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
