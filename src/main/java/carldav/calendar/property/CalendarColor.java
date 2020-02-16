package carldav.calendar.property;

import org.unitedinternet.cosmo.dav.property.StandardDavProperty;

import static carldav.CarldavConstants.CALENDAR_COLOR;

public class CalendarColor extends StandardDavProperty<String> {

    public CalendarColor(String color) {
        super(CALENDAR_COLOR, color);
    }
}
