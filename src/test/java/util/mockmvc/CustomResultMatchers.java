package util.mockmvc;

import static org.springframework.http.HttpHeaders.ETAG;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import org.hamcrest.Matcher;
import org.springframework.test.web.servlet.ResultMatcher;

/**
 * @author Kamill Sokol
 */
public class CustomResultMatchers {
    private CustomResultMatchers() {
        //private
    }

    public static ResultMatcher etag(Matcher<? super String> m) {
        return header().string(ETAG, m);
    }
}
