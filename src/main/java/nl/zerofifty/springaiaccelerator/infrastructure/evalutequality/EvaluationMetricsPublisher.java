package nl.zerofifty.springaiaccelerator.infrastructure.evalutequality;

import nl.zerofifty.springaiaccelerator.infrastructure.dao.EvaluationResponse;

public interface EvaluationMetricsPublisher {
    void publish(EvaluationResponse evaluation);
}
