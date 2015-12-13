package testutil.builder

/**
 * @author Kamill Sokol
 */
class GeneralData {

    static final String UUID = "59BC120D-E909-4A56-A70D-8E97914E51A3";

    static final String CALDAV_EVENT = """\
                        BEGIN:VCALENDAR
                        VERSION:2.0
                        X-WR-CALNAME:Work
                        PRODID:-//Apple Computer\\, Inc//iCal 2.0//EN
                        X-WR-RELCALID:21654AA6-F774-4918-80B8-F0C8CABC7737
                        X-WR-TIMEZONE:US/Pacific
                        CALSCALE:GREGORIAN
                        BEGIN:VTIMEZONE
                        TZID:US/Pacific
                        LAST-MODIFIED:20050812T212029Z
                        BEGIN:DAYLIGHT
                        DTSTART:20040404T100000
                        TZOFFSETTO:-0700
                        TZOFFSETFROM:+0000
                        TZNAME:PDT
                        END:DAYLIGHT
                        BEGIN:STANDARD
                        DTSTART:20041031T020000
                        TZOFFSETTO:-0800
                        TZOFFSETFROM:-0700
                        TZNAME:PST
                        END:STANDARD
                        BEGIN:DAYLIGHT
                        DTSTART:20050403T010000
                        TZOFFSETTO:-0700
                        TZOFFSETFROM:-0800
                        TZNAME:PDT
                        END:DAYLIGHT
                        BEGIN:STANDARD
                        DTSTART:20051030T020000
                        TZOFFSETTO:-0800
                        TZOFFSETFROM:-0700
                        TZNAME:PST
                        END:STANDARD
                        END:VTIMEZONE
                        BEGIN:VEVENT
                        DTSTART;TZID=US/Pacific:20050602T120000
                        LOCATION:Whoville
                        SUMMARY:all entities meeting
                        UID:59BC120D-E909-4A56-A70D-8E97914E51A3
                        SEQUENCE:4
                        DTSTAMP:20050520T014148Z
                        DURATION:PT1H
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()
}
