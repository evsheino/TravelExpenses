package wad.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import wad.interceptor.ForcePasswordChangeInterceptor;

/**
 * Created by nryytty@cs on 25.11.2014.
 */
@Component
public class ApplicationConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ForcePasswordChangeInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**", "/login", "/logout", "/authenticate", "/account/password/force", "/account/password/save");
    }

}
