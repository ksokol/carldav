package testutil.mockmvc

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.web.util.UriComponentsBuilder

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request
import static testutil.mockmvc.CaldavHttpMethod.PROPFIND
import static testutil.mockmvc.CaldavHttpMethod.REPORT

/**
 * @author Kamill Sokol
 */
public class CustomRequestBuilders {
    private CustomRequestBuilders() {
        //private
    }

    public static MockHttpServletRequestBuilder report(String urlTemplate, Object... urlVariables) {
        return request(REPORT.name(), UriComponentsBuilder.fromUriString(urlTemplate).buildAndExpand(urlVariables).encode().toUri())
    }

    public static MockHttpServletRequestBuilder propfind(String urlTemplate, Object... urlVariables) {
        return request(PROPFIND.name(), UriComponentsBuilder.fromUriString(urlTemplate).buildAndExpand(urlVariables).encode().toUri())
    }
}
