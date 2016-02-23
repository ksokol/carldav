package carldav;

import org.springframework.http.MediaType;

import java.nio.charset.Charset;

/**
 * @author Kamill Sokol
 */
public final class CarldavConstants {

    public static final String TEXT_CALENDAR = new MediaType("text", "calendar", Charset.forName("UTF-8")).toString();

    public static final String TEXT_HTML = new MediaType("text", "html", Charset.forName("UTF-8")).toString();
}
