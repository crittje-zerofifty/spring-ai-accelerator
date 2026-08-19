package nl.zerofifty.springaiaccelerator.application.port.output;

import org.springframework.ai.document.Document;
import java.util.List;

public interface SearchOutputPort {
    List<Document> search(String query);
}
