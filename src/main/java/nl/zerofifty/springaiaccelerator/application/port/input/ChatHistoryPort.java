package nl.zerofifty.springaiaccelerator.application.port.input;

import reactor.core.publisher.Flux;

public interface ChatHistoryPort {

    Flux<String> chat(final String prompt, final String chatId);
}
