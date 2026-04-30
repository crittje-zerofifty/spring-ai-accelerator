package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecureRagWithoutHistoryAdapterTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private StreamAdvisor advisor1;

    private SecureRagWithoutHistoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new SecureRagWithoutHistoryAdapter(chatClient, List.of(advisor1));
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenCallWithConfidential_thenProperlyConfiguresChatClient() {
        String prompt = "Hello AI";
        String securityLevel = "CONFIDENTIAL";

        var promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        var advisorSpecCaptor = ArgumentCaptor.forClass(Consumer.class);
        var streamResponseSpec = mock(ChatClient.StreamResponseSpec.class);

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.advisors(any(Consumer.class))).thenReturn(promptSpec);
        when(promptSpec.system(anyString())).thenReturn(promptSpec);
        when(promptSpec.user(prompt)).thenReturn(promptSpec);
        when(promptSpec.stream()).thenReturn(streamResponseSpec);
        when(streamResponseSpec.content()).thenReturn(Flux.just("AI response"));

        Flux<String> result = adapter.call(prompt, securityLevel);

        StepVerifier.create(result)
                .expectNext("AI response")
                .verifyComplete();

        verify(chatClient).prompt();
        verify(promptSpec).user(prompt);
        verify(promptSpec).system(contains("You are a helpful assistant"));
        verify(promptSpec).advisors(advisorSpecCaptor.capture());

        ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class);
        advisorSpecCaptor.getValue().accept(advisorSpec);

        verify(advisorSpec).advisors(advisor1);
        verify(advisorSpec).param(eq(QuestionAnswerAdvisor.FILTER_EXPRESSION), contains("CONFIDENTIAL"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenCallWithPublic_thenProperlyConfiguresChatClient() {
        String prompt = "Hello AI";
        String securityLevel = "PUBLIC";

        var promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        var advisorSpecCaptor = ArgumentCaptor.forClass(Consumer.class);
        var streamResponseSpec = mock(ChatClient.StreamResponseSpec.class);

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.advisors(any(Consumer.class))).thenReturn(promptSpec);
        when(promptSpec.system(anyString())).thenReturn(promptSpec);
        when(promptSpec.user(prompt)).thenReturn(promptSpec);
        when(promptSpec.stream()).thenReturn(streamResponseSpec);
        when(streamResponseSpec.content()).thenReturn(Flux.just("AI response"));

        adapter.call(prompt, securityLevel);

        verify(promptSpec).advisors(advisorSpecCaptor.capture());

        ChatClient.AdvisorSpec advisorSpec = mock(ChatClient.AdvisorSpec.class);
        advisorSpecCaptor.getValue().accept(advisorSpec);

        verify(advisorSpec).param(eq(QuestionAnswerAdvisor.FILTER_EXPRESSION), argThat(filter -> !((String)filter).contains("CONFIDENTIAL")));
    }
}
