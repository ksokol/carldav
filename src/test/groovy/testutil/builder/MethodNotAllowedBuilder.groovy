package testutil.builder

import carldav.CaldavHttpMethod
import org.springframework.http.HttpMethod

/**
 * @author Kamill Sokol
 */
public class MethodNotAllowedBuilder {
    private MethodNotAllowedBuilder() {
        //private
    }

    public static NotAllowed notAllowed(HttpMethod method) {
        return new NotAllowed(method.name())
    }

    public static NotAllowed notAllowed(CaldavHttpMethod method) {
        return new NotAllowed(method.name())
    }

    public static class NotAllowed {

        private final String method;

        NotAllowed(String method) {
            this.method = method;
        }

        public String onUserPrincipal() {
            return build("user principal")
        }

        public String onCollection() {
            return build("a collection")
        }

        private String build(String resource) {
            return """\
                    <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                      <cosmo:method-not-allowed>${method} not allowed for ${resource}</cosmo:method-not-allowed>
                    </D:error>"""
        }
    }
}