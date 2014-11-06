package wad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wad.domain.Authority;
import wad.domain.User;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {

}
