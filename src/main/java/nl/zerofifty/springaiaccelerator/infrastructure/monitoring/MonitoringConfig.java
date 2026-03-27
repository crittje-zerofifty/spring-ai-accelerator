package nl.zerofifty.springaiaccelerator.infrastructure.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
public class MonitoringConfig {

    @Bean
    @Profile("elk-monitoring")
    public StreamAdvisor kibanaMonitoringAdvisor() {
        return new KibanaMonitoringAdvisor();
    }

    @Bean
    @Profile("grafana-monitoring")
    @ConditionalOnClass(MeterRegistry.class)
    public StreamAdvisor grafanaMonitoringAdvisor(MeterRegistry registry) {
        return new GrafanaMonitoringAdvisor(registry);
    }
}
