package carldav;

import org.springframework.http.MediaType;

import java.nio.charset.Charset;

/**
 * @author Kamill Sokol
 */
public final class CarldavConstants {

    public static final MediaType TEXT_CALENDAR = new MediaType("text", "calendar", Charset.forName("UTF-8"));

    public static final String TEXT_CALENDAR_VALUE = TEXT_CALENDAR.toString();

    public static final MediaType TEXT_HTML = new MediaType("text", "html", Charset.forName("UTF-8"));

    public static final String TEXT_HTML_VALUE = TEXT_HTML.toString();

    public static final MediaType TEXT_VCARD = new MediaType("text", "vcard");
}
