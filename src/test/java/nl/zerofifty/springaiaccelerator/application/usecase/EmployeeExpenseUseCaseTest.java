package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.dto.ExpenseAuditResponse;
import nl.zerofifty.springaiaccelerator.application.port.output.EscalationOutputPort;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmOutputPort;
import nl.zerofifty.springaiaccelerator.application.port.output.SearchOutputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

class EmployeeExpenseUseCaseTest {

    private SearchOutputPort searchOutputPort;
    private LlmOutputPort llmOutputPort;
    private EscalationOutputPort escalationOutputPort;
    private EmployeeExpenseUseCase useCase;

    @BeforeEach
    void setUp() {
        searchOutputPort = mock(SearchOutputPort.class);
        llmOutputPort = mock(LlmOutputPort.class);
        escalationOutputPort = mock(EscalationOutputPort.class);
        useCase = new EmployeeExpenseUseCase(searchOutputPort, llmOutputPort, escalationOutputPort);
    }

    @Test
    void whenProcessExpense_thenSearchAndCallLlm() {
        String recipe = "Lunch at 30 EUR";
        List<Document> docs = List.of(new Document("Policy: Lunch capped at 25 EUR"));
        ExpenseAuditResponse response = new ExpenseAuditResponse(
                List.of(new ExpenseAuditResponse.ExpenseItem(1, "Lunch", 30.0, "REJECTED", "Over limit")),
                "Expense is over limit.",
                0.0
        );
        
        when(searchOutputPort.search(recipe)).thenReturn(docs);
        when(llmOutputPort.callWithContext(recipe, docs)).thenReturn(Mono.just(response));

        Mono<ExpenseAuditResponse> result = useCase.processExpense(recipe);

        StepVerifier.create(result)
                .expectNext(response)
                .verifyComplete();

        verify(searchOutputPort).search(recipe);
        verify(llmOutputPort).callWithContext(recipe, docs);
    }

    @Test
    void whenEscalatedInResponse_thenTriggerEscalation() {
        String recipe = "Jacuzzi at 500 EUR";
        List<Document> docs = List.of(new Document("Policy content", Map.of("relationships.escalates_to", "role_finance_manager")));
        ExpenseAuditResponse response = new ExpenseAuditResponse(
                List.of(new ExpenseAuditResponse.ExpenseItem(1, "Jacuzzi", 500.0, "ESCALATED", "High value")),
                "Jacuzzi needs review.",
                0.0
        );
        
        when(searchOutputPort.search(recipe)).thenReturn(docs);
        when(llmOutputPort.callWithContext(recipe, docs)).thenReturn(Mono.just(response));

        Mono<ExpenseAuditResponse> result = useCase.processExpense(recipe);

        StepVerifier.create(result)
                .expectNext(response)
                .verifyComplete();

        verify(escalationOutputPort).escalate(eq(recipe), anyString(), eq("role_finance_manager"));
    }
}
