package nl.zerofifty.springaiaccelerator.infrastructure.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileValidatorConfigTest {

    private ProfileValidatorConfig testable;
    private Environment env;

    @BeforeEach
    void setUp() {

        env = mock(Environment.class);

        testable = new ProfileValidatorConfig(env);
    }

    @Test
    void whenSecureRagIsNotActive_thenDoesNotThrow() {

        when(env.getActiveProfiles()).thenReturn(new String[]{"some-other-profile"});

        assertDoesNotThrow(testable::validateProfiles);
    }

    @Test
    void whenSecureRagIsActiveAndNoAuthProfile_thenThrowsIllegalStateException() {

        when(env.getActiveProfiles()).thenReturn(new String[]{"secure-rag"});

        assertThrows(IllegalStateException.class, testable::validateProfiles);
    }

    @Test
    void whenSecureRagIsActiveAndAuthProfileIsPresent_thenDoesNotThrow() {

        when(env.getActiveProfiles()).thenReturn(new String[]{"secure-rag", "auth-azure"});

        assertDoesNotThrow(testable::validateProfiles);
    }
}
