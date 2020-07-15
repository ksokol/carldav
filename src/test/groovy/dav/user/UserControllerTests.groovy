package dav.user

import org.junit.Test
import org.unitedinternet.cosmo.IntegrationTestSupport

import static calendar.DavDroidData.ADD_VEVENT_REQUEST1
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.startsWith
import static org.springframework.http.HttpHeaders.AUTHORIZATION
import static org.springframework.http.HttpHeaders.CONTENT_TYPE
import static org.springframework.http.MediaType.*
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import static testutil.TestUser.*
import static testutil.helper.Base64Helper.user
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomMediaTypes.TEXT_VCARD
import static testutil.mockmvc.CustomRequestBuilders.propfind
import static testutil.mockmvc.CustomResultMatchers.*

/**
 * @author Kamill Sokol
 */
class UserControllerTests extends IntegrationTestSupport {

    @Test
    void list() {
        mockMvc.perform(get("/user")
            .header(AUTHORIZATION, user(ADMIN, ADMIN_PASSWORD)))
            .andExpect(header().string(CONTENT_TYPE, is(APPLICATION_JSON_VALUE)))
            .andExpect(content().json('["test02@localhost.de","test01@localhost.de","root@localhost"]'))
    }

    @Test
    void createUser() {
        def request = """\
                        {
                            "email" : "${NEW_USER}",
                            "password" : "${NEW_USER_PASSWORD}"
                        }"""

        mockMvc.perform(post("/user")
                .contentType(APPLICATION_JSON)
                .content(request)
                .header(AUTHORIZATION, user(ADMIN, ADMIN_PASSWORD)))
                .andExpect(status().isCreated())
    }

    @Test
    void createSameUserTwice() {
        def request = """\
                        {
                            "email" : "${NEW_USER}",
                            "password" : "${NEW_USER_PASSWORD}"
                        }"""

        mockMvc.perform(post("/user")
                .contentType(APPLICATION_JSON)
                .content(request)
                .header(AUTHORIZATION, user(ADMIN, ADMIN_PASSWORD)))
                .andExpect(status().isCreated())

        mockMvc.perform(post("/user")
                .contentType(APPLICATION_JSON)
                .content(request)
                .header(AUTHORIZATION, user(ADMIN, ADMIN_PASSWORD)))
                .andExpect(status().isConflict())
    }

    @Test
    void createUserCheckCollections() {
        createUser()

        def request1 = """\
                        <propfind xmlns="DAV:" xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                            <prop>
                                <CARD:addressbook-home-set/>
                                <resourcetype/>
                                <displayname/>
                            </prop>
                        </propfind>"""

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                                <D:response>
                                    <D:href>/carldav/dav/new_user01@localhost.de/calendar/</D:href>
                                    <D:propstat>
                                        <D:prop>
                                            <CARD:addressbook-home-set xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                                                <D:href>/carldav/dav/new_user01@localhost.de/contacts</D:href>
                                            </CARD:addressbook-home-set>
                                            <D:displayname>calendarDisplayName</D:displayname>
                                            <D:resourcetype>
                                                <D:collection/>
                                                <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                            </D:resourcetype>
                                        </D:prop>
                                        <D:status>HTTP/1.1 200 OK</D:status>
                                    </D:propstat>
                                </D:response>
                            </D:multistatus>"""

        mockMvc.perform(propfind("/dav/{email}/calendar", NEW_USER)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("Depth", "0")
                .header(AUTHORIZATION, user(NEW_USER, NEW_USER_PASSWORD)))
                .andExpect(xml(response1))
    }

    @Test
    void addVEvent() {
        createUserCheckCollections()

        mockMvc.perform(put("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", NEW_USER)
                .contentType(TEXT_CALENDAR)
                .content(ADD_VEVENT_REQUEST1)
                .header(AUTHORIZATION, user(NEW_USER, NEW_USER_PASSWORD)))

        mockMvc.perform(get("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", NEW_USER)
                .header(AUTHORIZATION, user(NEW_USER, NEW_USER_PASSWORD)))
                .andExpect(textCalendarContentType())
                .andExpect(content().string(startsWith("BEGIN:VCALENDAR")))
    }

    @Test
    void addVTodo() {
        createUserCheckCollections()

        def request1 = """\
                    BEGIN:VCALENDAR
                    VERSION:2.0
                    PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 ical4android ical4j/2.x
                    BEGIN:VTODO
                    DTSTAMP:20151231T115937Z
                    UID:6f490b02-77d7-442e-abd3-1e0bb14c3259
                    CREATED:20151231T115922Z
                    LAST-MODIFIED:20151231T115922Z
                    SUMMARY:add vtodo
                    STATUS:NEEDS-ACTION
                    END:VTODO
                    END:VCALENDAR
                    """.stripIndent()

        mockMvc.perform(put("/dav/{email}/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics", NEW_USER)
                .contentType(TEXT_CALENDAR)
                .content(request1)
                .header(AUTHORIZATION, user(NEW_USER, NEW_USER_PASSWORD)))

        mockMvc.perform(get("/dav/{email}/calendar/6f490b02-77d7-442e-abd3-1e0bb14c3259.ics", NEW_USER)
                .header(AUTHORIZATION, user(NEW_USER, NEW_USER_PASSWORD)))
                .andExpect(textCalendarContentType())
                .andExpect(text(request1))
    }

    @Test
    void addVCard() {
        createUserCheckCollections()

        def request1 = """\
                        BEGIN:VCARD
                        VERSION:4.0
                        UID:d0f1d24e-2f4b-4318-b38c-92c6a0130c6a
                        PRODID:+//IDN bitfire.at//DAVdroid/0.9.1.2 vcard4android ez-vcard/0.9.6
                        FN:Name Prefix Name Middle Name Last Name\\, Name Suffix
                        N:Suffix;Name Prefix Name Middle Name Last Name;Name;;
                        X-PHONETIC-FIRST-NAME:name
                        X-PHONETIC-LAST-NAME:phonetic
                        TEL;TYPE=cell:746-63
                        TEL;TYPE=work:1111-1
                        EMAIL;TYPE=home:email@localhost
                        ORG:Company
                        TITLE:Title
                        IMPP:sip:sip
                        NICKNAME:Nickname
                        NOTE:Notes
                        REV:20160109T131938Z
                        END:VCARD
                        """.stripIndent()

        mockMvc.perform(put("/dav/{email}/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf", NEW_USER)
                .contentType(TEXT_VCARD)
                .content(request1)
                .header(AUTHORIZATION, user(NEW_USER, NEW_USER_PASSWORD)))
                .andExpect(status().isCreated())

        mockMvc.perform(get("/dav/{email}/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf", NEW_USER)
                .header(AUTHORIZATION, user(NEW_USER, NEW_USER_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(textCardContentType())
                .andExpect(text(request1))
    }
}
