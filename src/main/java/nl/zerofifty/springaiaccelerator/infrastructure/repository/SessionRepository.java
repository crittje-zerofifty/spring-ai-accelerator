package nl.zerofifty.springaiaccelerator.infrastructure.repository;

import nl.zerofifty.springaiaccelerator.infrastructure.dao.Session;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SessionRepository extends CrudRepository<Session, Long> {

    Optional<Session> findByChatId(String chatId);

}
