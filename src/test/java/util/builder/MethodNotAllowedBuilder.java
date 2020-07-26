package util.builder;

import org.springframework.http.HttpMethod;
import util.mockmvc.CaldavHttpMethod;

public class MethodNotAllowedBuilder {

    private MethodNotAllowedBuilder() {
        //private
    }

    public static NotAllowed notAllowed(HttpMethod method) {
        return new NotAllowed(method.name());
    }

    public static NotAllowed notAllowed(CaldavHttpMethod method) {
        return new NotAllowed(method.name());
    }

    public static class NotAllowed {

        private final String method;

        NotAllowed(String method) {
            this.method = method;
        }

        public String onUserPrincipal() {
            return build("user principal");
        }

        public String onUserPrincipalCollection() {
            return build("user principal collection");
        }

        public String onCollection() {
            return build("a collection");
        }

        public String onNonCollection() {
            return build("a non-collection resource");
        }

        public String onHomeCollection() {
            return build("home collection");
        }

        private String build(String resource) {
            return (
                    "<D:error xmlns:cosmo=\"http://osafoundation.org/cosmo/DAV\" xmlns:D=\"DAV:\">\n" +
                    "  <cosmo:method-not-allowed>" + method + " not allowed for "+ resource +"</cosmo:method-not-allowed>\n" +
                    "</D:error>"
                    );
        }
    }
}
