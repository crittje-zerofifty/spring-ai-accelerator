package nl.zerofifty.springaiaccelerator.infrastructure.adapter;

import nl.zerofifty.springaiaccelerator.application.port.output.EvaluateResultPort;
import nl.zerofifty.springaiaccelerator.infrastructure.dao.EvaluationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"eval-testing"})
public class EvaluateResultAdapter implements EvaluateResultPort {

    private static final Logger log = LoggerFactory.getLogger(EvaluateResultAdapter.class);

    private final ChatClient chatClient;

    public EvaluateResultAdapter(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public EvaluationResponse evaluate(String question, String context, String answer) {

        String evalPrompt = """
            You are an expert evaluator. Evaluate the following RAG response based on the context provided.
            
            Context: %s
            Question: %s
            Answer: %s
            
            Rate the answer on:
            1. Faithfulness (0-1): Is the answer derived ONLY from the context?
            2. Relevancy (0-1): Does the answer address the question?
            
            If the answer is not faithful, but the result indicates that it can't help with the question return 0.8 on faithfulness.
            Explain in the reasoning that it can't help with the question and therefore is faithful as it is acknowledged.
            
            Return ONLY a JSON object: {"faithfulness": 0.9, "relevancy": 1.0, "reasoning": "..."}
            """.formatted(context, question, answer);

        EvaluationResponse response = chatClient.prompt().user(evalPrompt).call()
                .entity(EvaluationResponse.class);

        if (log.isDebugEnabled()) {
            log.debug("Evaluation response: {}", response);
        }

        return response;
    }
}
