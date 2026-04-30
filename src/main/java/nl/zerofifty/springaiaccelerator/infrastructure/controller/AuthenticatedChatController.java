
package nl.zerofifty.springaiaccelerator.infrastructure.controller;

import nl.zerofifty.springaiaccelerator.application.port.input.AuthenticatedChatHistoryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * In case you use authentication but no history, the chatId defaults to 1. However, this value is not relevant for rest
 * of the process in that case.
 */
@RestController
@Profile({ "auth-azure", "auth-aws" })
public class AuthenticatedChatController {

    private final AuthenticatedChatHistoryPort authenticatedChatHistoryPort;

    public AuthenticatedChatController(AuthenticatedChatHistoryPort authenticatedChatHistoryPort) {
        this.authenticatedChatHistoryPort = authenticatedChatHistoryPort;
    }

    @GetMapping("/chat")
    public Flux<String> authenticatedChat(@RequestParam String prompt,
                                         @RequestParam(defaultValue = "1") String chatId,
                                         @AuthenticationPrincipal OidcUser user) {
        return authenticatedChatHistoryPort.chat(prompt, chatId, user.getEmail());
    }

}
