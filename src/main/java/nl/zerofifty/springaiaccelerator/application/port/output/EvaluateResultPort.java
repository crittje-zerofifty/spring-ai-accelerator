package nl.zerofifty.springaiaccelerator.application.port.output;

import nl.zerofifty.springaiaccelerator.infrastructure.dao.EvaluationResponse;

public interface EvaluateResultPort {

    EvaluationResponse evaluate(final String question, final String context, final String answer);
}
