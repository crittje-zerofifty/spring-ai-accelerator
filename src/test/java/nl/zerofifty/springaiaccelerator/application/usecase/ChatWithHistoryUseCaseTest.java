package nl.zerofifty.springaiaccelerator.application.usecase;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmHistoryClientPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatWithHistoryUseCaseTest {

    @Mock
    private LlmHistoryClientPort chatHistoryPort;

    @InjectMocks
    private ChatWithHistoryUseCase useCase;

    @Test
    void whenChatCalled_thenDelegatesToPort() {
        String prompt = "test prompt";
        String chatId = "test-chat-id";
        when(chatHistoryPort.call(prompt, chatId)).thenReturn(Flux.just("response"));

        Flux<String> result = useCase.chat(prompt, chatId);

        StepVerifier.create(result)
                .expectNext("response")
                .verifyComplete();

        verify(chatHistoryPort).call(prompt, chatId);
    }
}
