package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import nl.zerofifty.springaiaccelerator.application.annotation.EvaluateQuality;
import nl.zerofifty.springaiaccelerator.application.port.output.LlmSecureRagClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.ai.vectorstore.filter.converter.PrintFilterExpressionConverter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
@Profile("secure-rag")
public class SecureRagWithoutHistoryAdapter implements LlmSecureRagClient {

    private final ChatClient chatClient;
    private final List<StreamAdvisor> advisors;

    public SecureRagWithoutHistoryAdapter(ChatClient chatClient, List<StreamAdvisor> advisors) {
        this.chatClient = chatClient;
        this.advisors = advisors;
    }

    @Override
    public Flux<String> call(String prompt, String securityLevel) {
        return chatClient.prompt()
                .advisors(a -> {
                    advisors.forEach(a::advisors);
                    a.param(QuestionAnswerAdvisor.FILTER_EXPRESSION, getFilter(securityLevel));
                })
                .system("""
                        You are a helpful assistant. If you can't find the answer because you can't find it
                        in the docs provided you answer 'Sorry I cannot help you with that.'
                        Do not give any context about information you do no things about.
                        Just be short if you can't answer the question.
                        """)
                .user(prompt)
                .stream()
                .content();
    }

    private String getFilter(final String securityLevel) {

        FilterExpressionBuilder builder = new FilterExpressionBuilder();

        Filter.Expression expression;

        if ("CONFIDENTIAL".equals(securityLevel)) {
            expression = builder.in("level", "PUBLIC", "CONFIDENTIAL").build();
        } else {
            expression = builder.in("level", "PUBLIC").build();
        }

        return new PrintFilterExpressionConverter().convertExpression(expression);
    }
}
