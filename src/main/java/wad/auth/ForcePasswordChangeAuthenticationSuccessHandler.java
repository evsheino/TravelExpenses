package wad.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import wad.domain.User;
import wad.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by nryytty@cs on 25.11.2014.
 */
@Component
public class ForcePasswordChangeAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    public static final String SESSION_KEY_FORCE_PASSWORD_CHANGE = "forcePasswordChange";

    @Autowired
    private UserService userService;

    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        User user = userService.getCurrentUser();
        Boolean passwordExpired = (user.getPasswordExpired() != null ? user.getPasswordExpired() : Boolean.FALSE);
        request.getSession().setAttribute(SESSION_KEY_FORCE_PASSWORD_CHANGE, passwordExpired);
        super.onAuthenticationSuccess(request, response, authentication);
    }

}
