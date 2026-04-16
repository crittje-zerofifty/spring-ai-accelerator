package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.port.input.AuthenticatedChatHistoryPort;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmHistoryClientPort;
import nl.zerofifty.springaiaccelerator.infrastructure.dao.Session;
import nl.zerofifty.springaiaccelerator.infrastructure.repository.SessionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Service
@Profile("auth-azure")
public class AuthenticatedChatWithHistoryUseCase implements AuthenticatedChatHistoryPort {

    private final LlmHistoryClientPort chatHistoryPort;
    private final SessionRepository sessionRepository;

    public AuthenticatedChatWithHistoryUseCase(LlmHistoryClientPort chatHistoryPort, SessionRepository sessionRepository) {
        this.chatHistoryPort = chatHistoryPort;
        this.sessionRepository = sessionRepository;
    }

    @Override
    public Flux<String> chat(String prompt, String chatId, String userId) {
        // Your business logic here
        return Mono.fromCallable(() -> sessionRepository.findByChatId(chatId)
                            .map(session -> validateSessionOwnership(session, userId))
                            .orElseGet(() -> sessionRepository.save(new Session(userId, chatId)))
                )
                .subscribeOn(Schedulers.boundedElastic()) // Move blocking DB work to a separate thread pool
                .flatMapMany(session -> chatHistoryPort.call(prompt, chatId));
    }

    private Session validateSessionOwnership(Session session, String userId) {
        if (!session.getUserId().equals(userId)) {
             throw new SecurityException("User is not authorized for this chat session");
        }
        return session;
    }
}
