package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.annotation.EvaluateQuality;
import nl.zerofifty.springaiaccelerator.application.port.input.AuthenticatedChatHistoryPort;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmClientPort;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmHistoryClientPort;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmSecureRagClient;
import nl.zerofifty.springaiaccelerator.infrastructure.dao.Session;
import nl.zerofifty.springaiaccelerator.infrastructure.dao.UserPermission;
import nl.zerofifty.springaiaccelerator.infrastructure.repository.SessionRepository;
import nl.zerofifty.springaiaccelerator.infrastructure.repository.UserPermissionRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Service
@Profile("secure-rag")
@Primary
public class SecureRagUseCase implements AuthenticatedChatHistoryPort {

    private final UserPermissionRepository userPermissionRepository;
    private final LlmSecureRagClient llmSecureRagClient;

    public SecureRagUseCase(UserPermissionRepository userPermissionRepository, LlmSecureRagClient llmSecureRagClient) {
        this.userPermissionRepository = userPermissionRepository;
        this.llmSecureRagClient = llmSecureRagClient;
    }

    @EvaluateQuality
    @Override
    public Flux<String> chat(String prompt, String chatId, String userId) {
        // Your business logic here

        Optional<UserPermission> userPermissionOptional = userPermissionRepository.findByEmail("someone.awesome@company.com");

        if (userPermissionOptional.isEmpty()) {
            throw new SecurityException("User is not authorized to access documents");
        }

        UserPermission userPermission = userPermissionOptional.get();
        String securityLevel = userPermission.getSecurityLevel();

        return llmSecureRagClient.call(prompt, securityLevel);
    }
}
