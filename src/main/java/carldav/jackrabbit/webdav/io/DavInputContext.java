package carldav.jackrabbit.webdav.io;

import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.ValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.unitedinternet.cosmo.calendar.util.CalendarUtils;
import org.unitedinternet.cosmo.dav.BadRequestException;
import org.unitedinternet.cosmo.dav.CosmoDavException;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarDataException;
import org.unitedinternet.cosmo.dav.caldav.InvalidCalendarResourceException;
import org.unitedinternet.cosmo.dav.caldav.UnsupportedCalendarDataException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static carldav.CarldavConstants.TEXT_CALENDAR;
import static carldav.CarldavConstants.TEXT_VCARD;

public class DavInputContext {

    private final HttpServletRequest request;
    private final InputStream in;
    private Calendar calendar;

    public DavInputContext(HttpServletRequest request, InputStream in) {
        Assert.notNull(request, "request is null");
        this.request = request;
        this.in = in;
    }

    /**
     * @deprecated Use {@code #getCalendarString()} instead.
     */
    @Deprecated(since = "0.1")
    public Calendar getCalendar() {
        return getCalendar(false);
    }

    public String getCalendarString() {
        return getCalendar(false).toString();
    }

    public Calendar getCalendar(boolean allowCalendarWithMethod) {
        if (calendar != null) {
            return calendar;
        }

        if (!hasStream()) {
            return null;
        }

        if (getContentType() == null) {
            throw new BadRequestException("No media type specified");
        }

        var mediaType = MediaType.parseMediaType(getContentType());
        if (!mediaType.isCompatibleWith(TEXT_CALENDAR) && !mediaType.isCompatibleWith(TEXT_VCARD)) {
            throw new UnsupportedCalendarDataException(mediaType.toString());
        }

        try {
            var c = CalendarUtils.parseCalendar(getInputStream());
            c.validate(true);

            if (CalendarUtils.hasMultipleComponentTypes(c)) {
                throw new InvalidCalendarResourceException("Calendar object contains more than one type of component");
            }
            if (!allowCalendarWithMethod && c.getProperties().getProperty(Property.METHOD) != null) {
                throw new InvalidCalendarResourceException("Calendar object contains METHOD property");
            }

            calendar = c;
        } catch (IOException e) {
            throw new CosmoDavException(e);
        } catch (ParserException e) {
            throw new InvalidCalendarDataException("Failed to parse calendar object: " + e.getMessage());
        } catch (ValidationException e) {
            throw new InvalidCalendarDataException("Invalid calendar object: " + e.getMessage());
        }

        return calendar;
    }

    public boolean hasStream() {
        return in != null;
    }

    public InputStream getInputStream() {
        return in;
    }

    public String getContentType() {
        return request.getHeader(HttpHeaders.CONTENT_TYPE);
    }

    public String getCharset() {
        var characterEncoding = request.getCharacterEncoding();
        return characterEncoding != null ? characterEncoding : StandardCharsets.UTF_8.name();
    }
}
