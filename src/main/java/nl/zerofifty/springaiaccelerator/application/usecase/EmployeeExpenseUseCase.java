package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.dto.ExpenseAuditResponse;
import nl.zerofifty.springaiaccelerator.application.port.input.EmployeeExpenseInputPort;
import nl.zerofifty.springaiaccelerator.application.port.output.EscalationOutputPort;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmOutputPort;
import nl.zerofifty.springaiaccelerator.application.port.output.SearchOutputPort;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class EmployeeExpenseUseCase implements EmployeeExpenseInputPort {

    private final SearchOutputPort searchOutputPort;
    private final LlmOutputPort llmOutputPort;
    private final EscalationOutputPort escalationOutputPort;

    public EmployeeExpenseUseCase(SearchOutputPort searchOutputPort, 
                                 LlmOutputPort llmOutputPort,
                                 EscalationOutputPort escalationOutputPort) {
        this.searchOutputPort = searchOutputPort;
        this.llmOutputPort = llmOutputPort;
        this.escalationOutputPort = escalationOutputPort;
    }

    @Override
    public Mono<ExpenseAuditResponse> processExpense(String recipe) {
        List<Document> contextDocuments = searchOutputPort.search(recipe);
        
        return llmOutputPort.callWithContext(recipe, contextDocuments)
                .doOnNext(response -> {
                    boolean needsEscalation = response.items().stream()
                            .anyMatch(item -> "ESCALATED".equalsIgnoreCase(item.status()));
                    
                    if (needsEscalation) {
                        String managerRole = resolveManagerFromOkf(contextDocuments);
                        escalationOutputPort.escalate(recipe, "Policy violation requires review", managerRole);
                    }
                });
    }

    private String resolveManagerFromOkf(List<Document> documents) {
        return documents.stream()
                .filter(doc -> doc.getMetadata().containsKey("relationships.escalates_to"))
                .map(doc -> doc.getMetadata().get("relationships.escalates_to"))
                .map(Object::toString)
                .findFirst()
                .orElse("unknown_finance_manager");
    }
}
