package nl.zerofifty.springaiaccelerator.infrastructure.evalutequality;

import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

@Profile("eval-testing")
public class EvaluateQualityAdvisor implements StreamAdvisor {

    public @NonNull Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest request, StreamAdvisorChain chain) {
        return chain.nextStream(request)
                .flatMap(response -> {
                    if (response.chatResponse() == null) {
                        return Flux.just(response);
                    }

                    Object retrievedDocuments = response.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
                    if (retrievedDocuments == null) {
                        return Flux.just(response);
                    }

                    @SuppressWarnings("unchecked")
                    List<Document> docs = (List<Document>) retrievedDocuments;

                    if (docs.isEmpty()) {
                        return Flux.just(response);
                    }
                    String contextString =
                            docs.stream()
                                    .map(Document::getMetadata)
                                    .map(meta -> meta.get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS))
                                    .map(String::valueOf)
                                    .collect(Collectors.joining("\n---\n"));
                    return Flux.just(response)
                            .contextWrite(ctx -> ctx.put("retrieved_context", contextString));
                });
    }

    @Override
    public @NonNull String getName() {
        return "EvalTestingAdvisor";
    }

    @Override
    public int getOrder() {
        return 10;
    }
}

