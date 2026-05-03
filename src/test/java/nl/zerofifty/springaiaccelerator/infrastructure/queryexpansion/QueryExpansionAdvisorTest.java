package nl.zerofifty.springaiaccelerator.infrastructure.queryexpansion;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QueryExpansionAdvisorTest {

    @Mock
    private VectorStore vectorStore;

    @Mock
    private LlmClientPort llmClientPort;

    @Mock
    private StreamAdvisorChain streamChain;

    private QueryExpansionAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new QueryExpansionAdvisor(vectorStore, llmClientPort);
    }

    @Test
    @SuppressWarnings("unchecked")
    void whenAdviseStream_thenExpandsQueryAndRetrievesDocuments() {
        String originalQuestion = "What is the capital of France?";
        List<String> expandedQueries = List.of("Query 1", "Query 2");

        // Mock expansion LLM call
        when(llmClientPort.call(originalQuestion)).thenReturn(Flux.fromIterable(expandedQueries));

        // Mock VectorStore searches
        Document doc1 = new Document("Doc 1 content");
        Document doc2 = new Document("Doc 2 content");
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc1))
                .thenReturn(List.of(doc2))
                .thenReturn(List.of(doc1)); // Duplicates should be handled

        // Prepare request with an existing system message
        SystemMessage existingSystemMessage = new SystemMessage("Existing instruction");
        UserMessage userMessage = new UserMessage(originalQuestion);
        Prompt prompt = new Prompt(List.of(existingSystemMessage, userMessage));
        Map<String, Object> advisorsContext = new HashMap<>();
        ChatClientRequest request = new ChatClientRequest(prompt, advisorsContext);

        // Mock chain
        ChatClientResponse mockResponse = mock(ChatClientResponse.class);
        ArgumentCaptor<ChatClientRequest> requestCaptor = ArgumentCaptor.forClass(ChatClientRequest.class);
        when(streamChain.nextStream(requestCaptor.capture())).thenReturn(Flux.just(mockResponse));

        // Execute
        Flux<ChatClientResponse> resultFlux = advisor.adviseStream(request, streamChain);

        // Verify
        StepVerifier.create(resultFlux)
                .expectNext(mockResponse)
                .verifyComplete();

        // 1. Verify context population
        assertTrue(advisorsContext.containsKey(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS));
        List<Document> retrievedDocs = (List<Document>) advisorsContext.get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        assertEquals(2, retrievedDocs.size());
        assertTrue(retrievedDocs.contains(doc1));
        assertTrue(retrievedDocs.contains(doc2));

        // 2. Verify request transformation
        ChatClientRequest updatedRequest = requestCaptor.getValue();
        List<Message> instructions = updatedRequest.prompt().getInstructions();
        
        // Should have 3 messages now: New SystemMessage (context), Existing SystemMessage, UserMessage
        assertEquals(3, instructions.size());
        assertEquals(MessageType.SYSTEM, instructions.get(0).getMessageType());
        assertTrue(instructions.get(0).getText().contains("Doc 1 content"));
        assertTrue(instructions.get(0).getText().contains("Doc 2 content"));
        assertEquals("Existing instruction", instructions.get(1).getText());
        assertEquals(originalQuestion, instructions.get(2).getText());

        verify(vectorStore, times(3)).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void whenLlmReturnsNoQueries_thenOnlyOriginalQuestionIsUsed() {
        String originalQuestion = "Simple question";
        when(llmClientPort.call(originalQuestion)).thenReturn(Flux.empty());

        Document doc = new Document("Simple doc");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(doc));

        ChatClientRequest request = new ChatClientRequest(new Prompt(new UserMessage(originalQuestion)), new HashMap<>());
        
        ChatClientResponse mockResponse = mock(ChatClientResponse.class);
        ArgumentCaptor<ChatClientRequest> requestCaptor = ArgumentCaptor.forClass(ChatClientRequest.class);
        when(streamChain.nextStream(requestCaptor.capture())).thenReturn(Flux.just(mockResponse));

        StepVerifier.create(advisor.adviseStream(request, streamChain))
                .expectNext(mockResponse)
                .verifyComplete();

        verify(vectorStore, times(1)).similaritySearch(any(SearchRequest.class));
        
        ChatClientRequest updatedRequest = requestCaptor.getValue();
        assertTrue(updatedRequest.prompt().getInstructions().get(0).getText().contains("Simple doc"));
    }

    @Test
    void testMetadata() {
        assertEquals("QueryExpansionAdvisor", advisor.getName());
        assertEquals(-50, advisor.getOrder());
    }
}
