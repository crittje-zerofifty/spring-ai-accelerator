package nl.zerofifty.springaiaccelerator.application.port.output;

import reactor.core.publisher.Flux;

public interface LlmSecureRagClient {

    Flux<String> call(final String prompt, final String securityLevel);

}
