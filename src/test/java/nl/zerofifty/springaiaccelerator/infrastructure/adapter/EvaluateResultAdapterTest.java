package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import nl.zerofifty.springaiaccelerator.infrastructure.dao.EvaluationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EvaluateResultAdapterTest {

    @Mock
    private ChatClient chatClient;

    private EvaluateResultAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new EvaluateResultAdapter(chatClient);
    }

    @Test
    void whenEvaluate_thenProperlyConfiguresChatClient() {
        String question = "What is AI?";
        String context = "AI stands for Artificial Intelligence.";
        String answer = "AI is Artificial Intelligence.";
        
        EvaluationResponse expectedResponse = new EvaluationResponse(0.9, 1.0, "Good answer");

        var promptSpec = mock(ChatClient.ChatClientRequestSpec.class);
        var callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(promptSpec);
        when(promptSpec.user(anyString())).thenReturn(promptSpec);
        when(promptSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(EvaluationResponse.class)).thenReturn(expectedResponse);

        EvaluationResponse actualResponse = adapter.evaluate(question, context, answer);

        assertEquals(expectedResponse, actualResponse);

        verify(chatClient).prompt();
        verify(promptSpec).user(argThat((String prompt) -> 
            prompt.contains(question) && prompt.contains(context) && prompt.contains(answer)
        ));
        verify(callResponseSpec).entity(EvaluationResponse.class);
    }
}
