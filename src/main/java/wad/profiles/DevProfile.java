package wad.profiles;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import wad.domain.User;
import wad.repository.UserRepository;

@Configuration
@Profile(value = {"dev", "default"})
public class DevProfile {

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        User johnD = new User();
        johnD.setName("John Doe");
        johnD.setUsername("johnd");
        johnD.setPassword("johnd");

        userRepository.save(johnD);
    }
}

