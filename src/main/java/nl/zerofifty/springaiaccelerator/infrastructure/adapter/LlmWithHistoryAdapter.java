package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmHistoryClientPort;
import nl.zerofifty.springaiaccelerator.infrastructure.monitoring.KibanaMonitoringAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Profile("history")
public class LlmWithHistoryAdapter implements LlmHistoryClientPort {

    private final static String CHAT_MEMORY_CONVERSATION_ID = "chat_memory_conversation_id";

    private final ChatClient chatClient;

    public LlmWithHistoryAdapter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Flux<String> call(String prompt, String chatId) {
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID, chatId)
                        .advisors(new KibanaMonitoringAdvisor()))
                .stream()
                .content();
    }
}
