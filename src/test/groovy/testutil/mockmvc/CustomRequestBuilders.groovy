package testutil.mockmvc

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request
import static testutil.mockmvc.CaldavHttpMethod.*

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

    public static MockHttpServletRequestBuilder propfind(String urlTemplate, Object... urlVariables) {
        return request(PROPFIND.name(), urlTemplate, urlVariables);
    }

    public static MockHttpServletRequestBuilder proppatch(String urlTemplate, Object... urlVariables) {
        return request(PROPPATCH.name(), urlTemplate, urlVariables);
    }
}
