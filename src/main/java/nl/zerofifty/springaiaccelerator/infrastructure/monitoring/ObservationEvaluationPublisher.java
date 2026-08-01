package nl.zerofifty.springaiaccelerator.infrastructure.monitoring;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import nl.zerofifty.springaiaccelerator.infrastructure.dao.EvaluationResponse;
import nl.zerofifty.springaiaccelerator.infrastructure.evalutequality.EvaluationMetricsPublisher;
import org.springframework.stereotype.Component;

@Component
public class ObservationEvaluationPublisher implements EvaluationMetricsPublisher {

    private final ObservationRegistry observationRegistry;

    public ObservationEvaluationPublisher(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    @Override
    public void publish(EvaluationResponse evaluation) {
        Observation.createNotStarted("ai.evaluation", observationRegistry)
                .lowCardinalityKeyValue("ai.evaluation.outcome", evaluation.faithfulness() > 0.7 && evaluation.relevancy() > 0.7 ? "PASS" : "FAIL")
                .highCardinalityKeyValue("ai.evaluation.faithfulness", String.valueOf(evaluation.faithfulness()))
                .highCardinalityKeyValue("ai.evaluation.relevancy", String.valueOf(evaluation.relevancy()))
                .highCardinalityKeyValue("ai.evaluation.reasoning", evaluation.reasoning())
                .observe(() -> {
                    // This block represents the execution being observed.
                    // Since the evaluation is already done, we just complete the observation.
                });
    }
}
