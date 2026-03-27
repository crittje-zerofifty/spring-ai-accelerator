package nl.zerofifty.springaiaccelerator.application.port.output;

import reactor.core.publisher.Flux;

public interface LlmClientPort {

    Flux<String> call(final String prompt);
}
