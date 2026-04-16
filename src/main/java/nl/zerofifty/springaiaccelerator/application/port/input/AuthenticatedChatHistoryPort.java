package nl.zerofifty.springaiaccelerator.application.port.input;

import reactor.core.publisher.Flux;

public interface AuthenticatedChatHistoryPort {

    Flux<String> chat(final String prompt, final String chatId, final String userId);
}
