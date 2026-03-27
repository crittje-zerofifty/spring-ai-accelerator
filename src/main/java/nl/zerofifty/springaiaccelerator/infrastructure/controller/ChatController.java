
package nl.zerofifty.springaiaccelerator.infrastructure.controller;

import nl.zerofifty.springaiaccelerator.application.port.input.ChatPort;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * Controller to prompt without context of history. Each request and response follows the fire and forget principle
 */
@Profile("no-history")
@RestController
public class ChatController {

    private final ChatPort chatPort;

    public ChatController(ChatPort chatPort) {
        this.chatPort = chatPort;
    }

    @GetMapping("/chat")
    public Flux<String> chat(@RequestParam String prompt) {
        return chatPort.chat(prompt);
    }

}
