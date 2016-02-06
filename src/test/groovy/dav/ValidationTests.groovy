package dav

import org.junit.Ignore
import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.unitedinternet.cosmo.IntegrationTestSupport

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomMediaTypes.TEXT_VCARD
import static testutil.mockmvc.CustomResultMatchers.textXmlContentType
import static testutil.mockmvc.CustomResultMatchers.xml

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
class ValidationTests extends IntegrationTestSupport {

    @Test
    void displayNameVEvent() {
        def request1 = """\
                        BEGIN:VCALENDAR
                        PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN
                        VERSION:2.0
                        BEGIN:VEVENT
                        CREATED:20160206T122228Z
                        LAST-MODIFIED:20160206T122232Z
                        DTSTAMP:20160206T122232Z
                        UID:951bfa48-6f4a-43fc-acd9-473a4f5ae557
                        SUMMARY:
                        DTSTART;TZID=Europe/Berlin:20160206T140000
                        DTEND;TZID=Europe/Berlin:20160206T150000
                        END:VEVENT
                        END:VCALENDAR
                        """.stripIndent()

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:bad-request>may not be empty for property displayName actual value []</cosmo:bad-request>
                            </D:error>"""

        mockMvc.perform(put("/dav/{email}/calendar/951bfa48-6f4a-43fc-acd9-473a4f5ae557.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1))
                .andExpect(textXmlContentType())
                .andExpect(status().isBadRequest())
                .andExpect(xml(response1))
    }

    @Test
    void displayNameVTodo() {
        def request1 = """\
                        BEGIN:VCALENDAR
                        PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN
                        VERSION:2.0
                        BEGIN:VTODO
                        CREATED:20160206T132452Z
                        LAST-MODIFIED:20160206T132455Z
                        DTSTAMP:20160206T132455Z
                        UID:590b11bc-2ed0-44ec-9f76-72dc57e38015
                        SUMMARY:
                        CLASS:PUBLIC
                        END:VTODO
                        END:VCALENDAR
                        """.stripIndent()

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:bad-request>may not be empty for property displayName actual value []</cosmo:bad-request>
                            </D:error>"""

        mockMvc.perform(put("/dav/{email}/calendar/590b11bc-2ed0-44ec-9f76-72dc57e38015.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1))
                .andExpect(textXmlContentType())
                .andExpect(status().isBadRequest())
                .andExpect(xml(response1))
    }

    @Test
    void displayNameVJournal() {
        def request1 = """\
                        BEGIN:VCALENDAR
                        CALSCALE:GREGORIAN
                        PRODID:-//Ximian//NONSGML Evolution Calendar//EN
                        VERSION:2.0
                        BEGIN:VJOURNAL
                        UID:20160206T132723Z-30750-1000-2071-1@ksokol
                        DTSTAMP:20160205T174842Z
                        SUMMARY:
                        DTSTART;VALUE=DATE:20160206
                        CLASS:PUBLIC
                        SEQUENCE:1
                        CREATED:20160206T132728Z
                        LAST-MODIFIED:20160206T132728Z
                        END:VJOURNAL
                        END:VCALENDAR
                        """.stripIndent()

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:bad-request>may not be empty for property displayName actual value []</cosmo:bad-request>
                            </D:error>"""

        mockMvc.perform(put("/dav/{email}/calendar/20160206T132723Z-30750-1000-2071-1_ksokol.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(request1))
                .andExpect(textXmlContentType())
                .andExpect(status().isBadRequest())
                .andExpect(xml(response1))
    }

    @Ignore
    @Test
    void uidVCard() {
        def request1 = """\
                        BEGIN:VCARD
                        VERSION:3.0
                        URL:
                        TITLE:
                        ROLE:
                        X-EVOLUTION-MANAGER:
                        X-EVOLUTION-ASSISTANT:
                        NICKNAME:
                        X-EVOLUTION-SPOUSE:
                        NOTE:
                        FN:CONTACT
                        N:;CONTACT;;;
                        X-EVOLUTION-FILE-AS:CONTACT
                        X-EVOLUTION-BLOG-URL:
                        CALURI:
                        FBURL:
                        X-EVOLUTION-VIDEO-URL:
                        X-MOZILLA-HTML:FALSE
                        UID:
                        END:VCARD
                        """.stripIndent()

        def response1 = """\
                            <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                <cosmo:bad-request>may not be empty for property displayName actual value []</cosmo:bad-request>
                            </D:error>"""

        mockMvc.perform(put("/dav/{email}/contacts/9A5A5BA1-13C26FE2-8887CB2B.vcf", USER01)
                .contentType(TEXT_VCARD)
                .content(request1))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(textXmlContentType())
                .andExpect(status().isBadRequest())
                .andExpect(xml(response1))
    }
}
