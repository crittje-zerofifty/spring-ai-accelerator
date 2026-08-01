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
                .map(response -> {
                    // Hier worden de tokens in de metadata meegegeven in de laatste chunk.
                    // Door de ChatResponse te behouden, kan de Observation dit meten.
                    String content = response.getResult().getOutput().getText();
                    return content != null ? content : "";
                }).doOnSubscribe(s -> chatObservation.start())
                .doOnComplete(chatObservation::stop)
                .doOnError(chatObservation::error);
    }
}
