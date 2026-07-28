package com.househost.auth.application.port.out;

import com.househost.auth.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserPersistencePort {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<User> findFirstThreeByOrderByIdAsc();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
}
