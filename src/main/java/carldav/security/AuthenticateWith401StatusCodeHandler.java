package carldav.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.util.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Kamill Sokol
 */
public class AuthenticateWith401StatusCodeHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final String realmName;

    public AuthenticateWith401StatusCodeHandler(final String realmName) {
        Assert.notNull(realmName, "realmName must be specified");
        this.realmName = realmName;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        sendAuthentication(response);
    }

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {
        sendAuthentication(response);
    }

    private void sendAuthentication(HttpServletResponse response) {
        response.addHeader("WWW-Authenticate", String.format("Basic realm=\"%s\"", realmName));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
