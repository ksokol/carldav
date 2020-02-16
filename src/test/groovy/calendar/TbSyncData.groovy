package calendar

/**
 * @authos Kamill Sokol
 */
class TbSyncData {

    static String ADD_VEVENT_REQUEST1 = """\
                                            BEGIN:VCALENDAR
                                            PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN
                                            VERSION:2.0
                                            BEGIN:VTIMEZONE
                                            TZID:Europe/Berlin
                                            BEGIN:DAYLIGHT
                                            TZOFFSETFROM:+0100
                                            TZOFFSETTO:+0200
                                            TZNAME:CEST
                                            DTSTART:19700329T020000
                                            RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=3
                                            END:DAYLIGHT
                                            BEGIN:STANDARD
                                            TZOFFSETFROM:+0200
                                            TZOFFSETTO:+0100
                                            TZNAME:CET
                                            DTSTART:19701025T030000
                                            RRULE:FREQ=YEARLY;BYDAY=-1SU;BYMONTH=10
                                            END:STANDARD
                                            END:VTIMEZONE
                                            BEGIN:VEVENT
                                            CREATED:20200215T192814Z
                                            LAST-MODIFIED:20200215T192851Z
                                            DTSTAMP:20200215T192851Z
                                            UID:55d7d404-e3c5-4f73-843b-008a164d0b7d
                                            SUMMARY:test1
                                            ORGANIZER;RSVP=TRUE;PARTSTAT=ACCEPTED;ROLE=CHAIR:mailto:test1@localhost.de
                                            ATTENDEE;RSVP=TRUE;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT:mailto:test2
                                             @localhost.de
                                            DTSTART;TZID=Europe/Berlin:20190820T210000
                                            DTEND;TZID=Europe/Berlin:20190820T220000
                                            TRANSP:OPAQUE
                                            X-MOZ-SEND-INVITATIONS:FALSE
                                            END:VEVENT
                                            END:VCALENDAR
                                         """.stripIndent()

    static String ADD_VTODO_REQUEST1 = """\
                                           BEGIN:VCALENDAR
                                           PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN
                                           VERSION:2.0
                                           BEGIN:VTODO
                                           CREATED:20200215T194749Z
                                           LAST-MODIFIED:20200215T194754Z
                                           DTSTAMP:20200215T194754Z
                                           UID:8c7e686d-d84e-4926-8d15-7b474e76115f
                                           SUMMARY:todo1
                                           END:VTODO
                                           END:VCALENDAR
                                       """.stripIndent()

    static String ADD_VCARD_REQUEST1 = """\
                                           BEGIN:VCARD
                                           FN:test1
                                           N:;test1;;;
                                           UID:28a55da8-6de7-469c-8dcd-7bc88c9ec1da
                                           VERSION:3.0
                                           END:VCARD
                                       """.stripIndent()
}
