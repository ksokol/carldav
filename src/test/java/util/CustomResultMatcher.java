package util;

import static org.hamcrest.Matchers.is;

import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/**
 * @author Kamill Sokol
 */
public final class CustomResultMatcher {

    public static ResultMatcher contentFromFile(final String fileName) throws Exception {
        final String file = FileUtil.fromFile(fileName);
        return MockMvcResultMatchers.content().string(file);
    }
}
