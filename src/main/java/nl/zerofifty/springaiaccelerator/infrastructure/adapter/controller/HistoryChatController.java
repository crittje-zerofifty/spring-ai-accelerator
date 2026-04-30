
package nl.zerofifty.springaiaccelerator.infrastructure.adapter.controller;

import jakarta.annotation.Nonnull;
import nl.zerofifty.springaiaccelerator.application.port.input.AuthenticatedChatHistoryPort;
import nl.zerofifty.springaiaccelerator.application.port.input.ChatHistoryPort;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Controller to prompt with context of history
 */
@RestController
@Profile("history & !auth-azure")
public class HistoryChatController {

    private final ChatHistoryPort chatHistoryPort;

    public HistoryChatController(ChatHistoryPort chatHistoryPort) {
        this.chatHistoryPort = chatHistoryPort;
    }

    @GetMapping("/chat")
    public Flux<String> chat(@RequestParam String prompt,
                            @RequestParam @Nonnull String chatId) {
        return chatHistoryPort.chat(prompt, chatId);
    }

}
