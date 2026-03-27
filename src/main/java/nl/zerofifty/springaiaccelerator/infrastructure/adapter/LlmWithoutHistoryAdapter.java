package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmClientPort;
import nl.zerofifty.springaiaccelerator.infrastructure.monitoring.KibanaMonitoringAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Profile("no-history")
public class LlmWithoutHistoryAdapter implements LlmClientPort {

    private final ChatClient chatClient;

    public LlmWithoutHistoryAdapter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Flux<String> call(String prompt) {
        return chatClient.prompt()
                .advisors(a -> a.advisors(new KibanaMonitoringAdvisor()))
                .user(prompt)
                .stream()
                .content();
    }
}
