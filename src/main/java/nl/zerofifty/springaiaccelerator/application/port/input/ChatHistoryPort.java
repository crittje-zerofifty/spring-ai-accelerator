package nl.zerofifty.springaiaccelerator.application.port.input;

public interface ChatHistoryPort {

    String chat(final String prompt, final String chatId);
}
