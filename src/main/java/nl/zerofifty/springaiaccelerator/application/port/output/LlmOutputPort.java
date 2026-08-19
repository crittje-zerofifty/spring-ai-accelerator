package nl.zerofifty.springaiaccelerator.application.port.output;

import org.springframework.ai.document.Document;
import reactor.core.publisher.Flux;
import java.util.List;

public interface LlmOutputPort {
    Flux<String> callWithContext(String prompt, List<Document> context);
}
