package util.mockmvc;

import org.springframework.http.MediaType;

/**
 * @author Kamill Sokol
 */
public class CustomMediaTypes {
    private CustomMediaTypes() {
        //private
    }

    public static final String TEXT_CALENDAR_VALUE = "text/calendar";

    public static final MediaType TEXT_CALENDAR = MediaType.valueOf(TEXT_CALENDAR_VALUE);
}
