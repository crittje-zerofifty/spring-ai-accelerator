package nl.zerofifty.springaiaccelerator.infrastructure.config;

import nl.zerofifty.springaiaccelerator.infrastructure.adapter.EvaluateResultAdapter;
import nl.zerofifty.springaiaccelerator.infrastructure.evalutequality.EvaluateQualityAdvisor;
import nl.zerofifty.springaiaccelerator.infrastructure.evalutequality.EvaluationMetricsPublisher;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("eval-testing")
public class EvaluateQualityConfiguration {

    @Bean
    public StreamAdvisor evaluateQualityAdvisor(EvaluateResultAdapter adapter,
                                                List<EvaluationMetricsPublisher> publishers) {
        return new EvaluateQualityAdvisor(adapter, publishers);
    }

}
