package calendar

/**
 * @author Kamill Sokol
 */
class EvolutionData {

    static String ADD_VEVENT_RECURRENCE_REQUEST1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VEVENT
                        UID:20160123T135858Z-25739-1000-1796-13@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T145800
                        DTEND;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T155800
                        SEQUENCE:2
                        SUMMARY:event exception
                        CLASS:PUBLIC
                        TRANSP:OPAQUE
                        RRULE:FREQ=DAILY;COUNT=3
                        EXDATE;VALUE=DATE:20160124
                        CREATED:20160123T135931Z
                        LAST-MODIFIED:20160123T135931Z
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

    static String UPDATE_VEVENT_RECURRENCE_REQUEST1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VEVENT
                        UID:20160123T135858Z-25739-1000-1796-13@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T155900
                        DTEND;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T165900
                        SEQUENCE:2
                        SUMMARY:event exception
                        CLASS:PUBLIC
                        TRANSP:OPAQUE
                        RRULE;X-EVOLUTION-ENDDATE=20160125T145900Z:FREQ=DAILY;COUNT=3
                        EXDATE;VALUE=DATE:20160124
                        CREATED:20160123T135931Z
                        LAST-MODIFIED:20160123T135931Z
                        END:VEVENT
                        BEGIN:VEVENT
                        UID:20160123T135858Z-25739-1000-1796-13@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T155900
                        DTEND;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T165900
                        SEQUENCE:3
                        SUMMARY:event exception2
                        CLASS:PUBLIC
                        TRANSP:OPAQUE
                        CREATED:20160123T135931Z
                        LAST-MODIFIED:20160123T145948Z
                        RECURRENCE-ID;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T155900
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

    static String UPDATE_VEVENT_RECURRENCE_REQUEST2  = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VEVENT
                        UID:20160123T135858Z-25739-1000-1796-13@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T155900
                        DTEND;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T165900
                        SEQUENCE:2
                        SUMMARY:event exception
                        CLASS:PUBLIC
                        TRANSP:OPAQUE
                        RRULE;X-EVOLUTION-ENDDATE=20160125T145900Z:FREQ=DAILY;COUNT=3
                        EXDATE;VALUE=DATE:20160124
                        CREATED:20160123T135931Z
                        LAST-MODIFIED:20160123T135931Z
                        END:VEVENT
                        BEGIN:VEVENT
                        UID:20160123T135858Z-25739-1000-1796-13@localhost
                        DTSTAMP:20160119T173941Z
                        DTSTART;TZID=/freeassociation.sourceforge.net/Europe/Lisbon:
                         20160121T155900
                        SEQUENCE:3
                        SUMMARY:event exception2
                        CLASS:PUBLIC
                        TRANSP:OPAQUE
                        CREATED:20160123T135931Z
                        LAST-MODIFIED:20160123T145948Z
                        RECURRENCE-ID;TZID=/freeassociation.sourceforge.net/Europe/Berlin:
                         20160123T155900
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()
}
