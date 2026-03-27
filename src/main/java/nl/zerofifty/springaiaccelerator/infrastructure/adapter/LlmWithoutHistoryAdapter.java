package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmClientPort;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Profile("no-history")
public class LlmWithoutHistoryAdapter implements LlmClientPort {

    private final ChatClient chatClient;
    private final List<StreamAdvisor> advisors;

    public LlmWithoutHistoryAdapter(ChatClient chatClient, List<StreamAdvisor> advisors) {
        this.chatClient = chatClient;
        this.advisors = advisors;
    }

    @Override
    public Flux<String> call(String prompt) {
        return chatClient.prompt()
                .advisors(a -> advisors.forEach(a::advisors))
                .user(prompt)
                .stream()
                .content();
    }
}
