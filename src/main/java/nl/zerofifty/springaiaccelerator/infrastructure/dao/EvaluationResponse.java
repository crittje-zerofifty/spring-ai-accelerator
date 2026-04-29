package nl.zerofifty.springaiaccelerator.infrastructure.dao;

public record EvaluationResponse(double faithfulness, double relevancy, String reasoning) {
}
