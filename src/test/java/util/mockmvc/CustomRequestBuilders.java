package util.mockmvc;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;
import static util.mockmvc.CaldavHttpMethod.PROPFIND;
import static util.mockmvc.CaldavHttpMethod.REPORT;

public class CustomRequestBuilders {

    private CustomRequestBuilders() {
        //private
    }

    public static MockHttpServletRequestBuilder report(String urlTemplate, Object... urlVariables) {
        return request(REPORT.name(), fromUriString(urlTemplate).buildAndExpand(urlVariables).encode().toUri());
    }

    public static MockHttpServletRequestBuilder propfind(String urlTemplate, Object... urlVariables) {
        return request(PROPFIND.name(), fromUriString(urlTemplate).buildAndExpand(urlVariables).encode().toUri());
    }
}
