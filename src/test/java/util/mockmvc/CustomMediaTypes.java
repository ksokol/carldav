package util.mockmvc;

import org.springframework.http.MediaType;

public class CustomMediaTypes {

    private CustomMediaTypes() {
        //private
    }

    public static final String TEXT_CALENDAR_VALUE = "text/calendar";

    public static final String TEXT_VCARD_VALUE = "text/vcard";

    public static final MediaType TEXT_CALENDAR = MediaType.valueOf(TEXT_CALENDAR_VALUE);

    public static final MediaType TEXT_VCARD = MediaType.valueOf(TEXT_VCARD_VALUE);
}
