package wad.profiles;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

@Configuration
@Profile(value = {"dev", "default"})
public class DevProfile extends BaseProfile {

    @Override
    public String getTemplatePath() {
        return "/WEB-INF/templates/";
    }

    @Override
    public TemplateResolver getTemplateResolver() {
        return new ServletContextTemplateResolver();
    }

}

