
package nl.zerofifty.springaiaccelerator.infrastructure.controller;

import jakarta.annotation.Nonnull;
import nl.zerofifty.springaiaccelerator.application.port.input.AuthenticatedChatHistoryPort;
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
@Profile("auth-azure")
public class AuthenticatedHistoryChatController {

    private final AuthenticatedChatHistoryPort authenticatedChatHistoryPort;

    public AuthenticatedHistoryChatController(AuthenticatedChatHistoryPort authenticatedChatHistoryPort) {
        this.authenticatedChatHistoryPort = authenticatedChatHistoryPort;
    }

    @GetMapping("/chat")
    public Flux<String> authenticatedChat(@RequestParam String prompt,
                                         @RequestParam @Nonnull String chatId,
                                         @AuthenticationPrincipal OidcUser user) {
        return authenticatedChatHistoryPort.chat(prompt, chatId, user.getEmail());
    }

}
