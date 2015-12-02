package util.mockmvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * @author Kamill Sokol
 */
public class CustomRequestBuilders {
    private CustomRequestBuilders() {
        //private
    }

    public static MockHttpServletRequestBuilder report(String urlTemplate, Object... urlVariables) {
        return request("REPORT", urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder mkcalendar(String urlTemplate, Object... urlVariables) {
        return request("MKCALENDAR", urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder acl(String urlTemplate, Object... urlVariables) {
        return request("ACL", urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder propfind(String urlTemplate, Object... urlVariables) {
        return request("PROPFIND", urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder proppatch(String urlTemplate, Object... urlVariables) {
        return request("PROPPATCH", urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder copy(String urlTemplate, Object... urlVariables) {
        return request("COPY", urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder move(String urlTemplate, Object... urlVariables) {
        return request("MOVE", urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder mkticket(String urlTemplate, Object... urlVariables) {
        return request("MKTICKET", urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder delticket(String urlTemplate, Object... urlVariables) {
        return request("DELTICKET", urlTemplate, urlVariables);
    }
}
