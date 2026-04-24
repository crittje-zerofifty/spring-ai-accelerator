package nl.zerofifty.springaiaccelerator.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

/**
 * Note this class is only used to validate the presence of the required profiles in case one wants to use
 * secure rag. If you strip down the application to your needs, it is quite likely you can remove this class.
 */
@Configuration
public class ProfileValidatorConfig {

    private final Environment env;

    public ProfileValidatorConfig(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void validateProfiles() {
        List<String> activeProfiles = Arrays.asList(env.getActiveProfiles());

        if (activeProfiles.contains("secure-rag")) {
            boolean hasAuthProfile = activeProfiles.stream()
                    .anyMatch(profile -> profile.startsWith("auth-"));

            if (!hasAuthProfile) {
                throw new IllegalStateException(
                        "CRITICAL ERROR: Profile 'secure-rag' requires an authentication provider. " +
                                "Please activate either, for example, 'auth-azure' or 'basic-auth'."
                );
            }
        }
    }
}
