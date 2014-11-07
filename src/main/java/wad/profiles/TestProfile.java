package wad.profiles;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@Configuration
@Profile("test")
public class TestProfile extends BaseProfile {

    @Override
    public String getTemplatePath() {
        return "src/main/webapp/WEB-INF/templates/";
    }

    @Override
    public TemplateResolver getTemplateResolver() {
        return new FileTemplateResolver();
    }

}

