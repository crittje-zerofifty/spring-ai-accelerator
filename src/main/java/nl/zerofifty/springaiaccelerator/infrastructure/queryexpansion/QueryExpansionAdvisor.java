package nl.zerofifty.springaiaccelerator.infrastructure.queryexpansion;

import nl.zerofifty.springaiaccelerator.application.port.output.LlmClientPort;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Advisor that expands the user query into multiple related questions using an LLM.
 * These expanded queries are used to retrieve more relevant documents from the vector store.
 */
public class QueryExpansionAdvisor implements StreamAdvisor {

    private static final Logger log = LoggerFactory.getLogger(QueryExpansionAdvisor.class);

    private final VectorStore vectorStore;
    private final LlmClientPort llmClientPort;

    public QueryExpansionAdvisor(VectorStore vectorStore, LlmClientPort llmClientPort) {
        this.vectorStore = vectorStore;
        this.llmClientPort = llmClientPort;
    }

    @Override
    public @NonNull Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest request, @NonNull StreamAdvisorChain chain) {
        final String originalQuestion = request.prompt().getUserMessage().getText();

        return llmClientPort.call(originalQuestion)
                .collectList()
                .flatMapMany(expandedQueries -> expandAndRetrieveDocuments(request, chain, originalQuestion, expandedQueries));
    }

    private Flux<ChatClientResponse> expandAndRetrieveDocuments(ChatClientRequest request, StreamAdvisorChain chain, String originalQuestion, List<String> expandedQueries) {
        List<String> allQueries = new ArrayList<>(expandedQueries);
        allQueries.add(originalQuestion);

        List<Document> allDocuments = allQueries.stream()
                .flatMap(query -> {
                    log.debug("Retrieving documents for expanded query: {}", query);
                    return vectorStore.similaritySearch(
                            SearchRequest.builder()
                                    .query(query)
                                    .topK(2)
                                    .build()
                    ).stream();
                })
                .distinct()
                .collect(Collectors.toList());

        log.debug("Retrieved {} unique documents after query expansion", allDocuments.size());

        request.context().put(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS, allDocuments);

        ChatClientRequest updatedRequest = updateRequest(request, allDocuments);

        return chain.nextStream(updatedRequest);
    }

    @Override
    public @NonNull String getName() {
        return "QueryExpansionAdvisor";
    }

    @Override
    public int getOrder() {
        return -50;
    }

    private ChatClientRequest updateRequest(ChatClientRequest request, List<Document> allDocuments ) {
        String context = allDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        List<Message> messages = updateMessages(request, context);

        Prompt updatedPrompt = new Prompt(messages, request.prompt().getOptions());
        return new ChatClientRequest(updatedPrompt, request.context());
    }

    private @NonNull List<Message> updateMessages(ChatClientRequest request, String context) {
        String systemText = """
                Use the following context to answer the question.
                IMPORTANT: Follow the previously provided instructions regarding
                unanswerable questions and brevity.
                \n
                context: %s
                """.formatted(context);
        SystemMessage systemMessage = new SystemMessage(systemText);

        List<Message> messages = new ArrayList<>(request.prompt().getInstructions());
        messages.addFirst(systemMessage);
        return messages;
    }
}
