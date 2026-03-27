package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmHistoryClientPort;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Profile("history")
public class LlmWithHistoryAdapter implements LlmHistoryClientPort {

    private final ChatClient chatClient;
    private final List<Advisor> advisors;

    public LlmWithHistoryAdapter(ChatClient chatClient, List<Advisor> advisors) {
        this.chatClient = chatClient;
        this.advisors = advisors;
    }

    @Override
    public Flux<String> call(String prompt, String chatId) {
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> {
                    a.param("chat_memory_conversation_id", chatId);
                    advisors.forEach(a::advisors);
                })
                .stream()
                .content();
    }
}
