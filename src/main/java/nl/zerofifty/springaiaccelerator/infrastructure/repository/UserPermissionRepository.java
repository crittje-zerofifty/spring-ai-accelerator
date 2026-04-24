package nl.zerofifty.springaiaccelerator.infrastructure.repository;

import nl.zerofifty.springaiaccelerator.infrastructure.dao.UserPermission;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserPermissionRepository extends CrudRepository<UserPermission, Long> {

    Optional<UserPermission> findByEmail(String email);

}
