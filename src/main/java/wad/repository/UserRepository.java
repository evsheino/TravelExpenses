package wad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wad.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
