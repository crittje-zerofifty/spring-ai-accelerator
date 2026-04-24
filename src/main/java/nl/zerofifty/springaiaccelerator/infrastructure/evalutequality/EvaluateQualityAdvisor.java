package nl.zerofifty.springaiaccelerator.infrastructure.evalutequality;

import nl.zerofifty.springaiaccelerator.infrastructure.adapter.EvaluateResultAdapter;
import nl.zerofifty.springaiaccelerator.infrastructure.dao.EvaluationResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Profile("eval-testing")
public class EvaluateQualityAdvisor implements StreamAdvisor {

    private final EvaluateResultAdapter evaluateResultAdapter;
    private final List<EvaluationMetricsPublisher> publishers;

    public EvaluateQualityAdvisor(EvaluateResultAdapter evaluateResultAdapter, List<EvaluationMetricsPublisher> publishers) {
        this.evaluateResultAdapter = evaluateResultAdapter;
        this.publishers = publishers;
    }

    public @NonNull Flux<ChatClientResponse> adviseStream(@NonNull ChatClientRequest request, StreamAdvisorChain chain) {
        StringBuilder fullAnswerBuilder = new StringBuilder();
        final List<Document> retrievedDocs = new ArrayList<>(); // Use array to bypass closure restriction

        return chain.nextStream(request)
                .doOnNext(response -> captureResponseContent(response, fullAnswerBuilder, retrievedDocs))
                .doOnComplete(() -> evaluateAndPublish(request, fullAnswerBuilder, retrievedDocs));

    }

    private void captureResponseContent(ChatClientResponse response, StringBuilder fullAnswerBuilder, List<Document> retrievedDocs) {
        if (response.context().containsKey(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS)) {
            retrievedDocs.addAll((List<Document>) response.context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS));
        }
        String text = response.chatResponse().getResult().getOutput().getText();
        if (text != null) {
            fullAnswerBuilder.append(text);
        }
    }

    private void evaluateAndPublish(ChatClientRequest request, StringBuilder fullAnswerBuilder, List<Document> docs) {
        String prompt = request.prompt().getUserMessage().getText();
        String fullAnswer = fullAnswerBuilder.toString();

        String contextString = (docs == null || docs.isEmpty())
                ? "No context found"
                : docs.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));

        Mono.fromRunnable(() -> {
                    EvaluationResponse evalResult = evaluateResultAdapter.evaluate(prompt, contextString, fullAnswer);
                    publishers.forEach(p -> p.publish(evalResult));
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    @Override
    public @NonNull String getName() {
        return "EvalTestingAdvisor";
    }

    /*
      Should be lower than the other QuestionAnswerAdvisor for RAG.
      Otherwise the evaluation also includes the injected RAG context.
      Implying you've got the context twice for evaluation.
     */
    @Override
    public int getOrder() {
        return -10;
    }
}

