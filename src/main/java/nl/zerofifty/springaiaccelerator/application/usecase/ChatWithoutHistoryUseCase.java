package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.port.input.ChatPort;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmClientPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@Profile("no-history")
public class ChatWithoutHistoryUseCase implements ChatPort {

    private final LlmClientPort llmClientPort;

    public ChatWithoutHistoryUseCase(LlmClientPort llmClientPort) {
        this.llmClientPort = llmClientPort;
    }

    @Override
    public Flux<String> chat(final String prompt) {
        // Your business logic here
        return llmClientPort.call(prompt);
    }
}
