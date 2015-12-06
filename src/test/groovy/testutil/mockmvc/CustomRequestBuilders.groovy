package testutil.mockmvc;

import static carldav.CaldavHttpMethod.ACL;
import static carldav.CaldavHttpMethod.COPY;
import static carldav.CaldavHttpMethod.DELTICKET;
import static carldav.CaldavHttpMethod.MKCALENDAR;
import static carldav.CaldavHttpMethod.MKTICKET;
import static carldav.CaldavHttpMethod.MOVE;
import static carldav.CaldavHttpMethod.PROPFIND;
import static carldav.CaldavHttpMethod.PROPPATCH;
import static carldav.CaldavHttpMethod.REPORT;
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
        return request(REPORT.name(), urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder mkcalendar(String urlTemplate, Object... urlVariables) {
        return request(MKCALENDAR.name(), urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder acl(String urlTemplate, Object... urlVariables) {
        return request(ACL.name(), urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder propfind(String urlTemplate, Object... urlVariables) {
        return request(PROPFIND.name(), urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder proppatch(String urlTemplate, Object... urlVariables) {
        return request(PROPPATCH.name(), urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder copy(String urlTemplate, Object... urlVariables) {
        return request(COPY.name(), urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder move(String urlTemplate, Object... urlVariables) {
        return request(MOVE.name(), urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder mkticket(String urlTemplate, Object... urlVariables) {
        return request(MKTICKET.name(), urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder delticket(String urlTemplate, Object... urlVariables) {
        return request(DELTICKET.name(), urlTemplate, urlVariables);
    }
}
