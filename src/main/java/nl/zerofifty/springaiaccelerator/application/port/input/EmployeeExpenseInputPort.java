package nl.zerofifty.springaiaccelerator.application.port.input;

import reactor.core.publisher.Flux;

public interface EmployeeExpenseInputPort {
    Flux<String> processExpense(String recipe);
}
