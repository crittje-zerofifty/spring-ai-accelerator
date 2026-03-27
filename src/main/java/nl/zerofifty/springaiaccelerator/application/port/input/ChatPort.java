package nl.zerofifty.springaiaccelerator.application.port.input;

import reactor.core.publisher.Flux;

public interface ChatPort {

    Flux<String> chat(final String prompt);
}
