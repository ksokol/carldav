package carldav.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Kamill Sokol
 */
public class CustomBasicAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final String realmName;

    public CustomBasicAuthenticationEntryPoint(final String realmName) {
        Assert.notNull(realmName, "realmName must be specified");
        this.realmName = realmName;
    }

    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.addHeader("WWW-Authenticate", String.format("Basic realm=\"%s\"", realmName));
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
    }
}
