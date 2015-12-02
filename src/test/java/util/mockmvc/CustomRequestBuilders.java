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
}
