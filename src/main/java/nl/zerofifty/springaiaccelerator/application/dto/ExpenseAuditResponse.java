package nl.zerofifty.springaiaccelerator.application.dto;

import java.util.List;

/**
 * Represents the final structured audit response for a set of expenses.
 */
public record ExpenseAuditResponse(
    List<ExpenseItem> items,
    String summary,
    double totalApprovedAmount
) {
    /**
     * Individual expense item evaluation.
     */
    public record ExpenseItem(
        int index,
        String subject,
        double amount,
        String status, // APPROVED, REJECTED, ESCALATED
        String reason
    ) {}
}
