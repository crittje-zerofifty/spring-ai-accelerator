package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmHistoryClientPort;
import nl.zerofifty.springaiaccelerator.infrastructure.dao.Session;
import nl.zerofifty.springaiaccelerator.infrastructure.repository.SessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticatedChatWithHistoryUseCaseTest {

    @Mock
    private LlmHistoryClientPort chatHistoryPort;

    @Mock
    private SessionRepository sessionRepository;

    @InjectMocks
    private AuthenticatedChatWithHistoryUseCase useCase;

    private final String prompt = "Hello";
    private final String chatId = "chat-123";
    private final String userId = "user-456";

    @Test
    void whenSessionExistsAndUserIsAuthorized_thenReturnsChatFlux() {
        Session session = new Session(userId, chatId);
        when(sessionRepository.findByChatId(chatId)).thenReturn(Optional.of(session));
        when(chatHistoryPort.call(prompt, chatId)).thenReturn(Flux.just("Hi", " there"));

        Flux<String> result = useCase.chat(prompt, chatId, userId);

        StepVerifier.create(result)
                .expectNext("Hi")
                .expectNext(" there")
                .verifyComplete();

        verify(sessionRepository).findByChatId(chatId);
        verify(sessionRepository, never()).save(any());
        verify(chatHistoryPort).call(prompt, chatId);
    }

    @Test
    void whenSessionDoesNotExist_thenCreatesNewSessionAndReturnsChatFlux() {
        when(sessionRepository.findByChatId(chatId)).thenReturn(Optional.empty());
        when(sessionRepository.save(any(Session.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatHistoryPort.call(prompt, chatId)).thenReturn(Flux.just("Welcome"));

        Flux<String> result = useCase.chat(prompt, chatId, userId);

        StepVerifier.create(result)
                .expectNext("Welcome")
                .verifyComplete();

        verify(sessionRepository).findByChatId(chatId);
        verify(sessionRepository).save(argThat(session -> 
            session.getUserId().equals(userId) && session.getChatId().equals(chatId)
        ));
        verify(chatHistoryPort).call(prompt, chatId);
    }

    @Test
    void whenSessionExistsAndUserIsNotAuthorized_thenThrowsSecurityException() {
        Session session = new Session("other-user", chatId);
        when(sessionRepository.findByChatId(chatId)).thenReturn(Optional.of(session));

        Flux<String> result = useCase.chat(prompt, chatId, userId);

        StepVerifier.create(result)
                .expectError(SecurityException.class)
                .verify();

        verify(sessionRepository).findByChatId(chatId);
        verify(chatHistoryPort, never()).call(any(), any());
    }
}
