package wad.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import wad.auth.JpaAuthenticationProvider;

@Configuration
@EnableWebMvcSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/login", "/static*/**").permitAll()
                .anyRequest().authenticated();

        http.formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/authenticate")
                .defaultSuccessUrl("/index")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll();

        http.logout()
                .permitAll()
                .invalidateHttpSession(true);
        
        // Sulkee CSRF-suojauksen.
        http
            .csrf().disable();        

    }

    @Configuration
    protected static class AuthenticationConfiguration extends GlobalAuthenticationConfigurerAdapter {

        @Autowired
        private JpaAuthenticationProvider jpaAuthenticationProvider;

        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.authenticationProvider(jpaAuthenticationProvider);
            
            auth.inMemoryAuthentication()
                    .withUser("Teemu").password("sisaan").roles("USER");
            
            auth.inMemoryAuthentication()
                    .withUser("Tommi").password("esimies").roles("SUPERVISOR");
            
            auth.inMemoryAuthentication()
                    .withUser("Aku").password("password").roles("ADMIN");            
        }
    }
}
