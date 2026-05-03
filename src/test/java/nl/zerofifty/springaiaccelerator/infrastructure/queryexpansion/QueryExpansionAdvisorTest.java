package nl.zerofifty.springaiaccelerator.infrastructure.queryexpansion;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmClientPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        Document doc1 = new Document("Doc 1");
        Document doc2 = new Document("Doc 2");
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(doc1))
                .thenReturn(List.of(doc2))
                .thenReturn(List.of(doc1)); // Duplicates should be handled

        // Prepare request
        UserMessage userMessage = new UserMessage(originalQuestion);
        Prompt prompt = new Prompt(userMessage);
        Map<String, Object> advisorsContext = new HashMap<>();
        ChatClientRequest request = new ChatClientRequest(prompt, advisorsContext);

        // Mock chain
        ChatClientResponse mockResponse = mock(ChatClientResponse.class);
        when(streamChain.nextStream(any(ChatClientRequest.class))).thenReturn(Flux.just(mockResponse));

        // Execute
        Flux<ChatClientResponse> resultFlux = advisor.adviseStream(request, streamChain);

        // Verify
        StepVerifier.create(resultFlux)
                .expectNext(mockResponse)
                .verifyComplete();

        assertTrue(advisorsContext.containsKey(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS));
        List<Document> retrievedDocs = (List<Document>) advisorsContext.get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        // doc1 from Query 1, doc2 from Query 2, doc1 from original (distinct should keep 2)
        assertEquals(2, retrievedDocs.size());
        assertTrue(retrievedDocs.contains(doc1));
        assertTrue(retrievedDocs.contains(doc2));

        verify(vectorStore, times(3)).similaritySearch(any(SearchRequest.class));
    }
}
