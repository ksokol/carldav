package dav

import org.junit.Ignore
import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport

import static calendar.DavDroidData.ADD_VEVENT_REQUEST1
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomMediaTypes.TEXT_VCARD

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
public class CascadeTests extends IntegrationTestSupport {

    @Test
    public void deleteCalendar() {
        mockMvc.perform(put("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(ADD_VEVENT_REQUEST1))
                .andExpect(status().isCreated())

        mockMvc.perform(get("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01))
                .andExpect(status().isOk())

        mockMvc.perform(delete("/dav/{email}/calendar", USER01))

        mockMvc.perform(get("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01))
                .andExpect(status().isNotFound())
    }

    @Test
    void deleteAddressbook() {
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

        mockMvc.perform(put("/dav/{email}/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf", USER01)
                .contentType(TEXT_VCARD)
                .content(request1))
                .andExpect(status().isCreated())

        mockMvc.perform(get("/dav/{email}/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf", USER01))
                .andExpect(status().isOk())

        mockMvc.perform(delete("/dav/{email}/contacts", USER01))

        mockMvc.perform(get("/dav/{email}/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf", USER01))
                .andExpect(status().isNotFound())
    }

    @Ignore
    @Test
    void deleteUser() {
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

        mockMvc.perform(put("/dav/{email}/contacts/d0f1d24e-2f4b-4318-b38c-92c6a0130c6a.vcf", USER01)
                .contentType(TEXT_VCARD)
                .content(request1))
                .andExpect(status().isCreated())

        mockMvc.perform(put("/dav/{email}/calendar/e94d89d2-b195-4128-a9a8-be83a873deae.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(ADD_VEVENT_REQUEST1))
                .andExpect(status().isCreated())

        mockMvc.perform(delete("/dav/{email}", USER01))
            .andExpect(status().isNoContent())
    }
}
