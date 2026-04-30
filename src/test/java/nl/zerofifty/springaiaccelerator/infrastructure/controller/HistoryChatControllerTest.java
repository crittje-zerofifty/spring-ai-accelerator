package nl.zerofifty.springaiaccelerator.infrastructure.controller;

import nl.zerofifty.springaiaccelerator.application.port.input.ChatHistoryPort;
import nl.zerofifty.springaiaccelerator.infrastructure.adapter.controller.HistoryChatController;
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
class HistoryChatControllerTest {

    @Mock
    private ChatHistoryPort chatHistoryPort;

    @InjectMocks
    private HistoryChatController controller;

    @Test
    void whenChat_thenDelegatesToUseCase() {
        String prompt = "hello";
        String chatId = "chat1";
        
        when(chatHistoryPort.chat(prompt, chatId)).thenReturn(Flux.just("response"));

        Flux<String> result = controller.chat(prompt, chatId);

        StepVerifier.create(result)
                .expectNext("response")
                .verifyComplete();

        verify(chatHistoryPort).chat(prompt, chatId);
    }
}
