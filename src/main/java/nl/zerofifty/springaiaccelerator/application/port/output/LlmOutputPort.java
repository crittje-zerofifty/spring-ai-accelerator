package nl.zerofifty.springaiaccelerator.application.port.output;

import nl.zerofifty.springaiaccelerator.application.dto.ExpenseAuditResponse;
import org.springframework.ai.document.Document;
import reactor.core.publisher.Mono;
import java.util.List;

public interface LlmOutputPort {
    Mono<ExpenseAuditResponse> callWithContext(String prompt, List<Document> context);
}
