package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmOutputPort;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Profile("okf")
public class OkfLlmAdapter implements LlmOutputPort {

    private final ChatClient chatClient;
    private final List<StreamAdvisor> advisors;
    private final ObservationRegistry observationRegistry;

    public OkfLlmAdapter(ChatClient chatClient, List<StreamAdvisor> advisors, ObservationRegistry observationRegistry) {
        this.chatClient = chatClient;
        this.advisors = advisors;
        this.observationRegistry = observationRegistry;
    }

    @Override
    public Flux<String> callWithContext(String prompt, List<Document> context) {
        Observation observation = Observation.createNotStarted("gen_ai.okf.audit", observationRegistry)
                .contextualName("okf-expense-audit");

        String contextString = context.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        return chatClient.prompt()
                .advisors(a -> advisors.forEach(a::advisors))
                .system(s -> s.text("""
                                ### ROLE
                                   You are a strict Finance Audit Bot.
                                
                                   ### TASK
                                   1. Evaluate expenses against the provided POLICY CONTEXT.
                                   2. Output ONLY the evaluation results.
                                   3. DO NOT provide explanations, notes, or conversational text.
                                
                                   ### OUTPUT FORMAT
                                   - Expense [N]: [Subject]: [Amount] EUR: [APPROVED/REJECTED/ESCALATED]
                                
                                   Total expenses approved: [SUM] EUR
                                
                                   ### EXAMPLE
                                   - Expense 1: Travel Expense: 100 EUR: APPROVED
                                   - Expense 2: Alcoholic Expense: 10 EUR: REJECTED
                                
                                   Total expenses approved: 100 EUR
                                
                                   ### POLICY CONTEXT
                                   {context}
                                """)
                        .param("context", contextString))
                .user(prompt)
                .stream()
                .chatResponse()
                .mapNotNull(response -> Objects.requireNonNull(response.getResult()).getOutput().getText())
                .doOnSubscribe(s -> observation.start())
                .doOnComplete(observation::stop)
                .doOnError(observation::error)
                .defaultIfEmpty("No answer found.");
    }
}
