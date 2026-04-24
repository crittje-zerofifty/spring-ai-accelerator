package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.port.input.AuthenticatedChatHistoryPort;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmSecureRagClient;
import nl.zerofifty.springaiaccelerator.infrastructure.dao.UserPermission;
import nl.zerofifty.springaiaccelerator.infrastructure.repository.UserPermissionRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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

    @Override
    public Flux<String> chat(String prompt, String chatId, String userId) {
        return Mono.fromCallable(() -> userPermissionRepository.findByEmail(userId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(userPermissionOptional -> {
                    if (userPermissionOptional.isEmpty()) {
                        return Mono.error(new SecurityException("User is not authorized to access documents"));
                    }
                    String securityLevel = userPermissionOptional.get().getSecurityLevel();
                    return llmSecureRagClient.call(prompt, securityLevel);
                });
    }
}
