package wad.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import wad.domain.Authority;
import wad.domain.User;

import javax.transaction.Transactional;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    @Modifying
    @Transactional
    @Query("delete from Authority a where a.user = ?1")
    public void deleteAuthoritiesByUser(User user);

}
