package nl.zerofifty.springaiaccelerator.infrastructure.controller;

import nl.zerofifty.springaiaccelerator.application.port.input.AuthenticatedChatHistoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticatedHistoryChatControllerTest {

    @Mock
    private AuthenticatedChatHistoryPort authenticatedChatHistoryPort;

    @Mock
    private OidcUser oidcUser;

    @InjectMocks
    private AuthenticatedHistoryChatController controller;

    @Test
    void whenAuthenticatedChat_thenDelegatesToUseCase() {
        String prompt = "hello";
        String chatId = "chat1";
        String userEmail = "test@example.com";
        
        when(oidcUser.getEmail()).thenReturn(userEmail);
        when(authenticatedChatHistoryPort.chat(prompt, chatId, userEmail))
                .thenReturn(Flux.just("response"));

        Flux<String> result = controller.authenticatedChat(prompt, chatId, oidcUser);

        StepVerifier.create(result)
                .expectNext("response")
                .verifyComplete();

        verify(authenticatedChatHistoryPort).chat(prompt, chatId, userEmail);
    }
}
