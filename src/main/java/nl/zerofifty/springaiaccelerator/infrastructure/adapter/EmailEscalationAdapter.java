package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import nl.zerofifty.springaiaccelerator.application.port.output.EscalationOutputPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("okf")
public class EmailEscalationAdapter implements EscalationOutputPort {

    private static final Logger log = LoggerFactory.getLogger(EmailEscalationAdapter.class);

    @Override
    public void escalate(String expenseDetails, String reason, String targetManager) {
        log.info("""
                
                ================================================================================
                VIRTUAL EMAIL SENT
                ================================================================================
                TO: {}
                SUBJECT: Expense Escalation Required
                
                BODY:
                An expense claim has been flagged for manual review.
                
                REASON: {}
                
                EXPENSE DETAILS:
                {}
                
                ACTION: Please review this in the Finance Portal.
                ================================================================================
                """, targetManager, reason, expenseDetails);
    }
}
