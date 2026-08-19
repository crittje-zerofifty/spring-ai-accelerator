package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.port.input.EmployeeExpenseInputPort;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmOutputPort;
import nl.zerofifty.springaiaccelerator.application.port.output.SearchOutputPort;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class EmployeeExpenseUseCase implements EmployeeExpenseInputPort {

    private final SearchOutputPort searchOutputPort;
    private final LlmOutputPort llmOutputPort;

    public EmployeeExpenseUseCase(SearchOutputPort searchOutputPort, LlmOutputPort llmOutputPort) {
        this.searchOutputPort = searchOutputPort;
        this.llmOutputPort = llmOutputPort;
    }

    @Override
    public Flux<String> processExpense(String recipe) {
        List<Document> contextDocuments = searchOutputPort.search(recipe);
        return llmOutputPort.callWithContext(recipe, contextDocuments);
    }
}
