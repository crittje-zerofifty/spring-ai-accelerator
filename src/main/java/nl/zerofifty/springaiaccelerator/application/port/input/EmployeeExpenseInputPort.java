package nl.zerofifty.springaiaccelerator.application.port.input;

import nl.zerofifty.springaiaccelerator.application.dto.ExpenseAuditResponse;
import reactor.core.publisher.Mono;

public interface EmployeeExpenseInputPort {
    Mono<ExpenseAuditResponse> processExpense(String recipe);
}
