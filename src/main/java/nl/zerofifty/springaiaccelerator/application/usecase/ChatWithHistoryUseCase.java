package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.annotation.EvaluateQuality;
import nl.zerofifty.springaiaccelerator.application.port.input.ChatHistoryPort;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmHistoryClientPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Profile("history")
public class ChatWithHistoryUseCase implements ChatHistoryPort {

    private final LlmHistoryClientPort chatHistoryPort;

    public ChatWithHistoryUseCase(LlmHistoryClientPort chatHistoryPort) {
        this.chatHistoryPort = chatHistoryPort;
    }

    @EvaluateQuality
    @Override
    public Flux<String> chat(String prompt, String chatId) {
        // Your business logic here
        return chatHistoryPort.call(prompt, chatId);
    }
}
