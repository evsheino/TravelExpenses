package wad.interceptor;

import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import wad.auth.ForcePasswordChangeAuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by nryytty@cs on 25.11.2014.
 */
public class ForcePasswordChangeInterceptor extends HandlerInterceptorAdapter {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Boolean passwordExpired = (Boolean)request.getSession(true).getAttribute(ForcePasswordChangeAuthenticationSuccessHandler.SESSION_KEY_FORCE_PASSWORD_CHANGE);
        if(passwordExpired != null && passwordExpired) {
            RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
            redirectStrategy.sendRedirect(request, response, "/account/password/force");
            return false;
        }
        return true;
    }

}
