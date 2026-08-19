package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmOutputPort;
import nl.zerofifty.springaiaccelerator.application.port.output.SearchOutputPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.*;

class EmployeeExpenseUseCaseTest {

    private SearchOutputPort searchOutputPort;
    private LlmOutputPort llmOutputPort;
    private EmployeeExpenseUseCase useCase;

    @BeforeEach
    void setUp() {
        searchOutputPort = mock(SearchOutputPort.class);
        llmOutputPort = mock(LlmOutputPort.class);
        useCase = new EmployeeExpenseUseCase(searchOutputPort, llmOutputPort);
    }

    @Test
    void whenProcessExpense_thenSearchAndCallLlm() {
        String recipe = "Lunch at 30 EUR";
        List<Document> docs = List.of(new Document("Policy: Lunch capped at 25 EUR"));
        
        when(searchOutputPort.search(recipe)).thenReturn(docs);
        when(llmOutputPort.callWithContext(recipe, docs)).thenReturn(Flux.just("Expense is over limit."));

        Flux<String> result = useCase.processExpense(recipe);

        StepVerifier.create(result)
                .expectNext("Expense is over limit.")
                .verifyComplete();

        verify(searchOutputPort).search(recipe);
        verify(llmOutputPort).callWithContext(recipe, docs);
    }
}
