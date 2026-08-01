package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmHistoryClientPort;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

@Component
@Profile({"history", "auth-azure"})
public class LlmWithHistoryAdapter implements LlmHistoryClientPort {

    private final ChatClient chatClient;
    private final List<Advisor> advisors;
    private final ObservationRegistry observationRegistry;

    public LlmWithHistoryAdapter(ChatClient chatClient, List<Advisor> advisors, ObservationRegistry observationRegistry) {
        this.chatClient = chatClient;
        this.advisors = advisors;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public Flux<String> call(String prompt, String chatId) {

        Observation chatObservation = Observation.createNotStarted("gen_ai.chat.stream", observationRegistry)
                .highCardinalityKeyValue("app.chat.id", chatId).contextualName("chat-session");

        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> {
                    a.param("chat_memory_conversation_id", chatId);
                    advisors.forEach(a::advisors);
                })

                .stream()
                .chatResponse()
                .mapNotNull(response -> Objects.requireNonNull(response.getResult()).getOutput().getText())
                .doOnSubscribe(s -> chatObservation.start())
                .doOnComplete(chatObservation::stop)
                .doOnError(chatObservation::error);
    }
}
