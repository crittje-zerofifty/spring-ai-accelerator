package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmSecureRagClient;
import nl.zerofifty.springaiaccelerator.infrastructure.dao.UserPermission;
import nl.zerofifty.springaiaccelerator.infrastructure.repository.UserPermissionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecureRagUseCaseTest {

    @Mock
    private UserPermissionRepository userPermissionRepository;

    @Mock
    private LlmSecureRagClient llmSecureRagClient;

    @InjectMocks
    private SecureRagUseCase secureRagUseCase;

    @Test
    void whenUserHasPermission_thenReturnsFlux() {
        String prompt = "test prompt";
        String userId = "user@example.com";
        String securityLevel = "CONFIDENTIAL";
        UserPermission userPermission = new UserPermission(userId, securityLevel);

        when(userPermissionRepository.findByEmail(userId)).thenReturn(Optional.of(userPermission));
        when(llmSecureRagClient.call(prompt, securityLevel)).thenReturn(Flux.just("part1", "part2"));

        Flux<String> result = secureRagUseCase.chat(prompt, "chatId", userId);

        StepVerifier.create(result)
                .expectNext("part1")
                .expectNext("part2")
                .verifyComplete();
    }

    @Test
    void whenUserNotFound_thenReturnsError() {
        String userId = "unknown@example.com";

        when(userPermissionRepository.findByEmail(userId)).thenReturn(Optional.empty());

        Flux<String> result = secureRagUseCase.chat("prompt", "chatId", userId);

        StepVerifier.create(result)
                .expectError(SecurityException.class)
                .verify();
    }
}
