package nl.zerofifty.springaiaccelerator.infrastructure.monitoring;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import nl.zerofifty.springaiaccelerator.infrastructure.dao.EvaluationResponse;
import nl.zerofifty.springaiaccelerator.infrastructure.evalutequality.EvaluationMetricsPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("grafana-monitoring")
public class GrafanaEvaluationPublisher implements EvaluationMetricsPublisher {

    private final MeterRegistry registry;

    public GrafanaEvaluationPublisher(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void publish(EvaluationResponse evaluation) {

        DistributionSummary.builder("ai.evaluation.faithfulness.summary")
                .description("Distribution of faithfulness scores")
                .register(registry)
                .record(evaluation.faithfulness());

        DistributionSummary.builder("ai.evaluation.relevancy.summary")
                .description("Distribution of relevancy scores")
                .register(registry)
                .record(evaluation.relevancy());
    }
}
