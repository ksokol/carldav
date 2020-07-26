package util.builder;

public class GeneralData {

    public static final String UUID = "59BC120D-E909-4A56-A70D-8E97914E51A3";

    public static final String CALDAV_EVENT =
            "BEGIN:VCALENDAR\n" +
            "VERSION:2.0\n" +
            "X-WR-CALNAME:Work\n" +
            "PRODID:-//Apple Computer\\, Inc//iCal 2.0//EN\n" +
            "X-WR-RELCALID:21654AA6-F774-4918-80B8-F0C8CABC7737\n" +
            "X-WR-TIMEZONE:US/Pacific\n" +
            "CALSCALE:GREGORIAN\n" +
            "BEGIN:VTIMEZONE\n" +
            "TZID:US/Pacific\n" +
            "LAST-MODIFIED:20050812T212029Z\n" +
            "BEGIN:DAYLIGHT\n" +
            "DTSTART:20040404T100000\n" +
            "TZOFFSETTO:-0700\n" +
            "TZOFFSETFROM:+0000\n" +
            "TZNAME:PDT\n" +
            "END:DAYLIGHT\n" +
            "BEGIN:STANDARD\n" +
            "DTSTART:20041031T020000\n" +
            "TZOFFSETTO:-0800\n" +
            "TZOFFSETFROM:-0700\n" +
            "TZNAME:PST\n" +
            "END:STANDARD\n" +
            "BEGIN:DAYLIGHT\n" +
            "DTSTART:20050403T010000\n" +
            "TZOFFSETTO:-0700\n" +
            "TZOFFSETFROM:-0800\n" +
            "TZNAME:PDT\n" +
            "END:DAYLIGHT\n" +
            "BEGIN:STANDARD\n" +
            "DTSTART:20051030T020000\n" +
            "TZOFFSETTO:-0800\n" +
            "TZOFFSETFROM:-0700\n" +
            "TZNAME:PST\n" +
            "END:STANDARD\n" +
            "END:VTIMEZONE\n" +
            "BEGIN:VEVENT\n" +
            "DTSTART;TZID=US/Pacific:20050602T120000\n" +
            "LOCATION:Whoville\n" +
            "SUMMARY:all entities meeting\n" +
            "UID:59BC120D-E909-4A56-A70D-8E97914E51A3\n" +
            "SEQUENCE:4\n" +
            "DTSTAMP:20050520T014148Z\n" +
            "DURATION:PT1H\n" +
            "END:VEVENT\n" +
            "END:VCALENDAR";

    public static final String UUID_TODO = "f3bc6436-991a-4a50-88b1-f27838e615c1";

    public static final String CALDAV_TODO =
            "BEGIN:VCALENDAR\n" +
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\n" +
            "VERSION:2.0\n" +
            "BEGIN:VTIMEZONE\n" +
            "TZID:Europe/Berlin\n" +
            "BEGIN:DAYLIGHT\n" +
            "TZOFFSETFROM:+0100\n" +
            "TZOFFSETTO:+0200\n" +
            "TZNAME:CEST\n" +
            "DTSTART:19700329T020000\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n" +
            "END:DAYLIGHT\n" +
            "BEGIN:STANDARD\n" +
            "TZOFFSETFROM:+0200\n" +
            "TZOFFSETTO:+0100\n" +
            "TZNAME:CET\n" +
            "DTSTART:19701025T030000\n" +
            "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n" +
            "END:STANDARD\n" +
            "END:VTIMEZONE\n" +
            "BEGIN:VTODO\n" +
            "CREATED:20151213T203529Z\n" +
            "LAST-MODIFIED:20151213T203552Z\n" +
            "DTSTAMP:20151213T203552Z\n" +
            "UID:f3bc6436-991a-4a50-88b1-f27838e615c1\n" +
            "SUMMARY:test task\n" +
            "STATUS:NEEDS-ACTION\n" +
            "RRULE:FREQ=WEEKLY\n" +
            "DTSTART;TZID=Europe/Berlin:20151213T220000\n" +
            "DUE;TZID=Europe/Berlin:20151214T220000\n" +
            "PERCENT-COMPLETE:25\n" +
            "BEGIN:VALARM\n" +
            "ACTION:DISPLAY\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\n" +
            "DESCRIPTION:Default Mozilla Description\n" +
            "END:VALARM\n" +
            "END:VTODO\n" +
            "END:VCALENDAR";


    public static final String UUID_EVENT2 = "18f0e0e5-4e1e-4e0d-b317-0d861d3e575c";

    public static final String CALDAV_EVENT2 =
            "BEGIN:VCALENDAR\n" +
            "PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\n" +
            "VERSION:2.0\n" +
            "BEGIN:VEVENT\n" +
            "CREATED:20151215T212053Z\n" +
            "LAST-MODIFIED:20151215T212127Z\n" +
            "DTSTAMP:20151215T212127Z\n" +
            "UID:18f0e0e5-4e1e-4e0d-b317-0d861d3e575c\n" +
            "SUMMARY:title\n" +
            "ORGANIZER;RSVP=TRUE;PARTSTAT=ACCEPTED;ROLE=CHAIR:mailto:kamill@sokol-web.d\n" +
            " e\n" +
            "ATTENDEE;RSVP=TRUE;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT:attende\n" +
            "RRULE:FREQ=DAILY\n" +
            "X-MOZ-LASTACK:20151215T212127Z\n" +
            "DTSTART;VALUE=DATE:20151206\n" +
            "DTEND;VALUE=DATE:20151207\n" +
            "TRANSP:TRANSPARENT\n" +
            "LOCATION:location\n" +
            "DESCRIPTION:description\n" +
            "X-MOZ-SEND-INVITATIONS:TRUE\n" +
            "X-MOZ-SEND-INVITATIONS-UNDISCLOSED:FALSE\n" +
            "X-MOZ-GENERATION:1\n" +
            "BEGIN:VALARM\n" +
            "ACTION:DISPLAY\n" +
            "TRIGGER;VALUE=DURATION:-PT15M\n" +
            "DESCRIPTION:Default Mozilla Description\n" +
            "END:VALARM\n" +
            "END:VEVENT\n" +
            "END:VCALENDAR";
}
