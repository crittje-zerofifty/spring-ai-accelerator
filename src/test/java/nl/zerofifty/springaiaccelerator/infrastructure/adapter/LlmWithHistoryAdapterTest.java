package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LlmWithHistoryAdapterTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private Advisor advisor1;

    private LlmWithHistoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LlmWithHistoryAdapter(chatClient, List.of(advisor1));
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenCall_thenProperlyConfiguresChatClient() {
        String prompt = "Hello AI";
        String chatId = "chat-123";

        var promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        var advisorSpecCaptor = ArgumentCaptor.forClass(Consumer.class);
        var streamResponseSpec = mock(ChatClient.StreamResponseSpec.class);

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.user(prompt)).thenReturn(promptSpec);
        when(promptSpec.advisors(any(Consumer.class))).thenReturn(promptSpec);
        when(promptSpec.stream()).thenReturn(streamResponseSpec);
        when(streamResponseSpec.content()).thenReturn(Flux.just("AI response"));

        Flux<String> result = adapter.call(prompt, chatId);

        StepVerifier.create(result)
                .expectNext("AI response")
                .verifyComplete();

        verify(chatClient).prompt();
        verify(promptSpec).user(prompt);
        verify(promptSpec).advisors(advisorSpecCaptor.capture());

        ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class);
        advisorSpecCaptor.getValue().accept(advisorSpec);

        verify(advisorSpec).param("chat_memory_conversation_id", chatId);
        verify(advisorSpec).advisors(advisor1);
    }
}
