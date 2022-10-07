package calendar

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

  static String UPDATE_VEVENT_RECURRENCE_REQUEST2 = """\
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

  static String VCARD = """\
                        BEGIN:VCARD
                        VERSION:3.0
                        URL:home page
                        TITLE:
                        ROLE:
                        X-EVOLUTION-MANAGER:manager
                        X-EVOLUTION-ASSISTANT:assistant
                        NICKNAME:Nickname
                        BDAY:1992-05-13
                        X-EVOLUTION-ANNIVERSARY:2016-01-14
                        X-EVOLUTION-SPOUSE:
                        NOTE:notes
                        FN:Mr. First Middle Last II
                        N:Last;First;Middle;Mr.;II
                        X-EVOLUTION-FILE-AS:Last\\, First
                        CATEGORIES:Birthday,Business
                        X-EVOLUTION-BLOG-URL:blog
                        CALURI:calendar
                        FBURL:free/busy
                        X-EVOLUTION-VIDEO-URL:video chat
                        X-MOZILLA-HTML:TRUE
                        EMAIL;TYPE=WORK:work@email
                        EMAIL;TYPE=HOME:home@email
                        EMAIL;TYPE=OTHER:other@email
                        TEL;TYPE=WORK,VOICE:business pohne
                        TEL;TYPE=HOME,VOICE:home phone
                        TEL;TYPE=CAR:car phone
                        TEL;TYPE=VOICE:other phone
                        X-SIP;TYPE=WORK:work sip
                        X-SIP;TYPE=HOME:home sip
                        X-SIP;TYPE=OTHER:other sip
                        X-AIM;X-EVOLUTION-UI-SLOT=1:aim
                        X-SKYPE;X-EVOLUTION-UI-SLOT=2:skype
                        ADR;TYPE=WORK:;;address work;;;;country
                        LABEL;TYPE=WORK:address work\\ncountry
                        ADR;TYPE=HOME:;;address home;city;;;
                        LABEL;TYPE=HOME:address home\\ncity
                        UID:EE0F1E48-114E3062-76210FF9
                        END:VCARD
                        """.stripIndent()
}
