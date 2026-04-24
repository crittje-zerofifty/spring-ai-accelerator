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
        return chain.nextStream(request)
                .collectList() // Verzamel alle chunks van de stream
                .flatMapMany(responses -> {
                    if (responses.isEmpty()) return Flux.empty();

                    String prompt = request.prompt().getUserMessage().getText();

                    // 2. Haal de Context op (uit de metadata van de eerste response)
                    List<Document> docs =
                            (List<Document>) responses.getFirst().context().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
                    String contextString = (docs == null || docs.isEmpty())
                            ? "No context found"
                            : docs.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));

                    String fullAnswer = responses.stream()
                            .map(res -> res.chatResponse().getResult().getOutput().getText())
                            .collect(Collectors.joining());

                    EvaluationResponse evalResult = evaluateResultAdapter.evaluate(prompt, contextString, fullAnswer);

                    publishers.forEach(p -> p.publish(evalResult));

                    return Flux.fromIterable(responses);
                });
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

