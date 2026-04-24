package nl.zerofifty.springaiaccelerator.infrastructure.config;

import nl.zerofifty.springaiaccelerator.infrastructure.evalutequality.EvaluateQualityAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("eval-testing")
public class EvaluateQualityConfiguration {

    @Bean
    public StreamAdvisor evaluateQualityAdvisor() {
        return new EvaluateQualityAdvisor();
    }

}
