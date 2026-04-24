package nl.zerofifty.springaiaccelerator.infrastructure.monitoring;

import nl.zerofifty.springaiaccelerator.infrastructure.dao.EvaluationResponse;
import nl.zerofifty.springaiaccelerator.infrastructure.evalutequality.EvaluationMetricsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
@Profile("elk-monitoring")
public class KibanaEvaluationPublisher implements EvaluationMetricsPublisher {

    private static final Logger log = LoggerFactory.getLogger(KibanaEvaluationPublisher.class);

    @Override
    public void publish(EvaluationResponse evaluation) {
        log.info("AI Evaluation Result",
                kv("faithfulness", evaluation.faithfulness()),
                kv("relevancy", evaluation.relevancy()),
                kv("reasoning", evaluation.reasoning()));
    }
}
