package nl.zerofifty.springaiaccelerator.application.port.output;

public interface EscalationOutputPort {
    void escalate(String expenseDetails, String reason, String targetManager);
}
