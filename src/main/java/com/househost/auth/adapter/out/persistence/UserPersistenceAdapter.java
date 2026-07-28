package com.househost.auth.adapter.out.persistence;

import com.househost.auth.adapter.out.persistence.entity.UserPersistenceMapper;
import com.househost.auth.application.port.out.UserPersistencePort;
import com.househost.auth.domain.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceAdapter implements UserPersistencePort {
    private final UserJpaRepository repository;
    public UserPersistenceAdapter(UserJpaRepository repository) { this.repository = repository; }
    public User save(User user) { return UserPersistenceMapper.toDomain(repository.save(UserPersistenceMapper.toEntity(user))); }
    public Optional<User> findById(Long id) { return repository.findById(id).map(UserPersistenceMapper::toDomain); }
    public Optional<User> findByEmail(String email) { return repository.findByEmail(email).map(UserPersistenceMapper::toDomain); }
    public List<User> findFirstThreeByOrderByIdAsc() { return repository.findFirst3ByOrderByIdAsc().stream().map(UserPersistenceMapper::toDomain).toList(); }
    public boolean existsByUsername(String username) { return repository.existsByUsername(username); }
    public boolean existsByEmail(String email) { return repository.existsByEmail(email); }
    public boolean existsByUsernameAndIdNot(String username, Long id) { return repository.existsByUsernameAndIdNot(username, id); }
    public boolean existsByEmailAndIdNot(String email, Long id) { return repository.existsByEmailAndIdNot(email, id); }
}
