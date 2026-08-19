package nl.zerofifty.springaiaccelerator.infrastructure.adapter.controller;

import nl.zerofifty.springaiaccelerator.application.dto.ExpenseAuditResponse;
import nl.zerofifty.springaiaccelerator.application.port.input.EmployeeExpenseInputPort;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/expense")
@Profile("okf")
public class EmployeeExpenseController {

    private final EmployeeExpenseInputPort employeeExpenseInputPort;

    public EmployeeExpenseController(EmployeeExpenseInputPort employeeExpenseInputPort) {
        this.employeeExpenseInputPort = employeeExpenseInputPort;
    }

    @PostMapping("/process")
    public Mono<ExpenseAuditResponse> processExpense(@RequestBody String recipe) {
        return employeeExpenseInputPort.processExpense(recipe);
    }
}
