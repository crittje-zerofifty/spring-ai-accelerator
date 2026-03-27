package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.port.input.ChatHistoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("history")
public class ChatWithHistoryUseCase implements ChatHistoryPort {

    private final ChatHistoryPort chatHistoryPort;

    public ChatWithHistoryUseCase(ChatHistoryPort chatHistoryPort) {
        this.chatHistoryPort = chatHistoryPort;
    }

    @Override
    public String chat(String prompt, String chatId) {
        // Your business logic here
        return chatHistoryPort.chat(prompt, chatId);
    }
}
