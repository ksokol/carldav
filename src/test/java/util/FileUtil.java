package util;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * @author Kamill Sokol
 */
public class FileUtil {

    public static String fromFile(String fileName) {
        try {
            return IOUtils.toString(new ClassPathResource(fileName).getInputStream());
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }
}
