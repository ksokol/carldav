package calendar

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport

import static calendar.TbSyncData.*
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat
import static org.springframework.http.HttpHeaders.ETAG
import static org.springframework.http.MediaType.APPLICATION_XML
import static org.springframework.http.MediaType.TEXT_XML
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.helper.XmlHelper.getetag
import static testutil.mockmvc.CustomMediaTypes.TEXT_CALENDAR
import static testutil.mockmvc.CustomMediaTypes.TEXT_VCARD
import static testutil.mockmvc.CustomRequestBuilders.propfind
import static testutil.mockmvc.CustomRequestBuilders.report
import static testutil.mockmvc.CustomResultMatchers.*
import static testutil.xmlunit.XmlMatcher.equalXml

/**
 * @author Kamill Sokol
 */
@WithUserDetails(USER01)
class TbSyncTests extends IntegrationTestSupport {

    def currentEtag

    @Test
    void fetchingEmptyCalendarFirstTimeFromCalDavResource() {
        def request1 = """
                        <d:propfind xmlns:d="DAV:">
                            <d:prop>
                                <d:current-user-principal />
                            </d:prop>
                        </d:propfind>
                        """

        def response1 = """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                            <D:propstat>
                              <D:prop>
                                <D:current-user-principal>
                                  <D:href>/carldav/principals/users/${USER01}</D:href>
                                </D:current-user-principal>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/dav/{email}/calendar", USER01)
                .contentType(TEXT_XML)
                .content(request1)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

        def request2 = """
                        <d:propfind xmlns:d="DAV:" xmlns:apple="http://apple.com/ns/ical/" xmlns:cs="http://calendarserver.org/ns/">
                          <d:prop>
                            <d:current-user-privilege-set/>
                            <d:resourcetype/>
                            <d:displayname/>
                            <apple:calendar-color/>
                            <cs:source/>
                          </d:prop>
                        </d:propfind>
                        """

        def response2 = """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/</D:href>
                            <D:propstat>
                              <D:prop>
                                <cs:source xmlns:cs="http://calendarserver.org/ns/"/>
                                <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                </D:resourcetype>
                                <D:displayname>homeCollection</D:displayname>
                                <D:current-user-privilege-set>
                                  <D:privilege>
                                    <D:read/>
                                  </D:privilege>
                                  <D:privilege>
                                    <D:write/>
                                  </D:privilege>
                                </D:current-user-privilege-set>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                            <D:propstat>
                              <D:prop>
                                <cs:source xmlns:cs="http://calendarserver.org/ns/"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                </D:resourcetype>
                                <D:displayname>calendarDisplayName</D:displayname>
                                <D:current-user-privilege-set>
                                  <D:privilege>
                                    <D:read/>
                                  </D:privilege>
                                  <D:privilege>
                                    <D:write/>
                                  </D:privilege>
                                </D:current-user-privilege-set>
                                <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/">#000000</apple:calendar-color>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                            <D:propstat>
                              <D:prop>
                                <cs:source xmlns:cs="http://calendarserver.org/ns/"/>
                                <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <CARD:addressbook xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
                                </D:resourcetype>
                                <D:displayname>contactDisplayName</D:displayname>
                                <D:current-user-privilege-set>
                                  <D:privilege>
                                    <D:read/>
                                  </D:privilege>
                                  <D:privilege>
                                    <D:write/>
                                  </D:privilege>
                                </D:current-user-privilege-set>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/dav/{email}", USER01)
                .contentType(TEXT_XML)
                .content(request2)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))

        def request3 = """
                        <d:propfind xmlns:d="DAV:" xmlns:cal="urn:ietf:params:xml:ns:caldav" xmlns:cs="http://calendarserver.org/ns/">
                          <d:prop>
                            <cal:calendar-home-set/>
                            <d:group-membership/>
                          </d:prop>
                        </d:propfind>
                       """

        def response3 = """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/principals/users/${USER01}</D:href>
                            <D:propstat>
                              <D:prop>
                                <d:group-membership xmlns:d="DAV:"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <C:calendar-home-set xmlns:C="urn:ietf:params:xml:ns:caldav">
                                  <D:href>/carldav/dav/${USER01}</D:href>
                                </C:calendar-home-set>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/principals/users/{email}", USER01)
                .contentType(TEXT_XML)
                .content(request3)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response3))

        def request4 = """
                        <d:propfind xmlns:d="DAV:" xmlns:apple="http://apple.com/ns/ical/" xmlns:cs="http://calendarserver.org/ns/">
                          <d:prop>
                            <d:current-user-privilege-set/>
                            <d:resourcetype/>
                            <d:displayname/>
                            <apple:calendar-color/>
                            <cs:source/>
                          </d:prop>
                        </d:propfind>
                       """

        def response4 = """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/</D:href>
                            <D:propstat>
                              <D:prop>
                                <cs:source xmlns:cs="http://calendarserver.org/ns/"/>
                                <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                </D:resourcetype>
                                <D:displayname>homeCollection</D:displayname>
                                <D:current-user-privilege-set>
                                  <D:privilege>
                                    <D:read/>
                                  </D:privilege>
                                  <D:privilege>
                                    <D:write/>
                                  </D:privilege>
                                </D:current-user-privilege-set>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                            <D:propstat>
                              <D:prop>
                                <cs:source xmlns:cs="http://calendarserver.org/ns/"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                </D:resourcetype>
                                <D:displayname>calendarDisplayName</D:displayname>
                                <D:current-user-privilege-set>
                                  <D:privilege>
                                    <D:read/>
                                  </D:privilege>
                                  <D:privilege>
                                    <D:write/>
                                  </D:privilege>
                                </D:current-user-privilege-set>
                                <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/">#000000</apple:calendar-color>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                            <D:propstat>
                              <D:prop>
                                <cs:source xmlns:cs="http://calendarserver.org/ns/"/>
                                <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <CARD:addressbook xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
                                </D:resourcetype>
                                <D:displayname>contactDisplayName</D:displayname>
                                <D:current-user-privilege-set>
                                  <D:privilege>
                                    <D:read/>
                                  </D:privilege>
                                  <D:privilege>
                                    <D:write/>
                                  </D:privilege>
                                </D:current-user-privilege-set>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/dav/{email}", USER01)
                .contentType(TEXT_XML)
                .content(request4)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response4))

        def request5 = """
                        <d:propfind xmlns:d="DAV:">
                          <d:prop>
                            <d:resourcetype/>
                            <d:displayname/>
                          </d:prop>
                        </d:propfind>
                        """

        def response5 = """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                </D:resourcetype>
                                <D:displayname>calendarDisplayName</D:displayname>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request5)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response5))

        def request6 = """
                        <D:propfind xmlns:D="DAV:" xmlns:CS="http://calendarserver.org/ns/" xmlns:C="urn:ietf:params:xml:ns:caldav">
                          <D:prop>
                            <D:resourcetype/>
                            <D:owner/>
                            <D:current-user-principal/>
                            <D:supported-report-set/>
                            <C:supported-calendar-component-set/>
                            <CS:getctag/>
                          </D:prop>
                        </D:propfind>
                       """

        def response6= """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                            <D:propstat>
                              <D:prop>
                                <D:owner/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                </D:resourcetype>
                                <C:supported-calendar-component-set xmlns:C="urn:ietf:params:xml:ns:caldav">
                                  <C:comp name="VEVENT"/>
                                  <C:comp name="VJOURNAL"/>
                                  <C:comp name="VTODO"/>
                                </C:supported-calendar-component-set>
                                <D:supported-report-set>
                                  <D:supported-report>
                                    <D:report>
                                      <C:calendar-multiget xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                    </D:report>
                                  </D:supported-report>
                                  <D:supported-report>
                                    <D:report>
                                      <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                    </D:report>
                                  </D:supported-report>
                                </D:supported-report-set>
                                <CS:getctag xmlns:CS="http://calendarserver.org/ns/">157565ba8b0d3652b027c868d554f914</CS:getctag>
                                <D:current-user-principal>
                                  <D:href>/carldav/principals/users/${USER01}</D:href>
                                </D:current-user-principal>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request6)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response6))


        mockMvc.perform(options("/dav/{email}/", USER01))
                .andExpect(status().isOk())
                .andExpect(header().string("Allow", "OPTIONS, GET, HEAD, PROPFIND"))
                .andExpect(header().string("DAV", "1, 3, addressbook, calendar-access"))

        def request7 = """
                        <D:propfind xmlns:D="DAV:">
                          <D:prop>
                            <D:getcontenttype/>
                            <D:resourcetype/>
                            <D:getetag/>
                          </D:prop>
                        </D:propfind>
                        """

        def response7 = """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                            <D:propstat>
                              <D:prop>
                                <D:getcontenttype/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                </D:resourcetype>
                                <D:getetag>"157565ba8b0d3652b027c868d554f914"</D:getetag>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request7)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response7))
    }

    @Test
    void fetchingEmptyCalendarFirstTimeFromCardDavResource() {
        def request1 = """
                        <d:propfind xmlns:d="DAV:">
                            <d:prop>
                                <d:current-user-principal />
                            </d:prop>
                        </d:propfind>
                        """

        def response1 = """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                            <D:propstat>
                              <D:prop>
                                <D:current-user-principal>
                                  <D:href>/carldav/principals/users/${USER01}</D:href>
                                </D:current-user-principal>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(TEXT_XML)
                .content(request1)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

        def request2 = """
                        <d:propfind xmlns:d="DAV:" xmlns:apple="http://apple.com/ns/ical/" xmlns:cs="http://calendarserver.org/ns/">
                          <d:prop>
                            <d:current-user-privilege-set/>
                            <d:resourcetype/>
                            <d:displayname/>
                            <apple:calendar-color/>
                            <cs:source/>
                          </d:prop>
                        </d:propfind>
                        """

        def response2 = """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/</D:href>
                            <D:propstat>
                              <D:prop>
                                <cs:source xmlns:cs="http://calendarserver.org/ns/"/>
                                <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                </D:resourcetype>
                                <D:displayname>homeCollection</D:displayname>
                                <D:current-user-privilege-set>
                                  <D:privilege>
                                    <D:read/>
                                  </D:privilege>
                                  <D:privilege>
                                    <D:write/>
                                  </D:privilege>
                                </D:current-user-privilege-set>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                            <D:propstat>
                              <D:prop>
                                <cs:source xmlns:cs="http://calendarserver.org/ns/"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                </D:resourcetype>
                                <D:displayname>calendarDisplayName</D:displayname>
                                <D:current-user-privilege-set>
                                  <D:privilege>
                                    <D:read/>
                                  </D:privilege>
                                  <D:privilege>
                                    <D:write/>
                                  </D:privilege>
                                </D:current-user-privilege-set>
                                <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/">#000000</apple:calendar-color>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                            <D:propstat>
                              <D:prop>
                                <cs:source xmlns:cs="http://calendarserver.org/ns/"/>
                                <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <CARD:addressbook xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
                                </D:resourcetype>
                                <D:displayname>contactDisplayName</D:displayname>
                                <D:current-user-privilege-set>
                                  <D:privilege>
                                    <D:read/>
                                  </D:privilege>
                                  <D:privilege>
                                    <D:write/>
                                  </D:privilege>
                                </D:current-user-privilege-set>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/dav/{email}", USER01)
                .contentType(TEXT_XML)
                .content(request2)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))

        def request3 = """
                        <d:propfind xmlns:d="DAV:" xmlns:cal="urn:ietf:params:xml:ns:caldav" xmlns:cs="http://calendarserver.org/ns/">
                          <d:prop>
                            <cal:calendar-home-set/>
                            <d:group-membership/>
                          </d:prop>
                        </d:propfind>
                       """

        def response3 = """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/principals/users/${USER01}</D:href>
                            <D:propstat>
                              <D:prop>
                                <d:group-membership xmlns:d="DAV:"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <C:calendar-home-set xmlns:C="urn:ietf:params:xml:ns:caldav">
                                  <D:href>/carldav/dav/${USER01}</D:href>
                                </C:calendar-home-set>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/principals/users/{email}", USER01)
                .contentType(TEXT_XML)
                .content(request3)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response3))

        def request4 = """
                        <d:propfind xmlns:d="DAV:" xmlns:apple="http://apple.com/ns/ical/" xmlns:cs="http://calendarserver.org/ns/">
                          <d:prop>
                            <d:current-user-privilege-set/>
                            <d:resourcetype/>
                            <d:displayname/>
                            <apple:calendar-color/>
                            <cs:source/>
                          </d:prop>
                        </d:propfind>
                       """

        def response4 = """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/</D:href>
                            <D:propstat>
                              <D:prop>
                                <cs:source xmlns:cs="http://calendarserver.org/ns/"/>
                                <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                </D:resourcetype>
                                <D:displayname>homeCollection</D:displayname>
                                <D:current-user-privilege-set>
                                  <D:privilege>
                                    <D:read/>
                                  </D:privilege>
                                  <D:privilege>
                                    <D:write/>
                                  </D:privilege>
                                </D:current-user-privilege-set>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                            <D:propstat>
                              <D:prop>
                                <cs:source xmlns:cs="http://calendarserver.org/ns/"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                </D:resourcetype>
                                <D:displayname>calendarDisplayName</D:displayname>
                                <D:current-user-privilege-set>
                                  <D:privilege>
                                    <D:read/>
                                  </D:privilege>
                                  <D:privilege>
                                    <D:write/>
                                  </D:privilege>
                                </D:current-user-privilege-set>
                                <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/">#000000</apple:calendar-color>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                            <D:propstat>
                              <D:prop>
                                <cs:source xmlns:cs="http://calendarserver.org/ns/"/>
                                <apple:calendar-color xmlns:apple="http://apple.com/ns/ical/"/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <CARD:addressbook xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
                                </D:resourcetype>
                                <D:displayname>contactDisplayName</D:displayname>
                                <D:current-user-privilege-set>
                                  <D:privilege>
                                    <D:read/>
                                  </D:privilege>
                                  <D:privilege>
                                    <D:write/>
                                  </D:privilege>
                                </D:current-user-privilege-set>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/dav/{email}", USER01)
                .contentType(TEXT_XML)
                .content(request4)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response4))

        def request5 = """
                        <d:propfind xmlns:d="DAV:">
                          <d:prop>
                            <d:resourcetype/>
                            <d:displayname/>
                          </d:prop>
                        </d:propfind>
                        """

        def response5 = """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                </D:resourcetype>
                                <D:displayname>calendarDisplayName</D:displayname>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request5)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response5))

        def request6 = """
                        <D:propfind xmlns:D="DAV:" xmlns:CS="http://calendarserver.org/ns/" xmlns:C="urn:ietf:params:xml:ns:caldav">
                          <D:prop>
                            <D:resourcetype/>
                            <D:owner/>
                            <D:current-user-principal/>
                            <D:supported-report-set/>
                            <C:supported-calendar-component-set/>
                            <CS:getctag/>
                          </D:prop>
                        </D:propfind>
                       """

        def response6= """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                            <D:propstat>
                              <D:prop>
                                <D:owner/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                </D:resourcetype>
                                <C:supported-calendar-component-set xmlns:C="urn:ietf:params:xml:ns:caldav">
                                  <C:comp name="VEVENT"/>
                                  <C:comp name="VJOURNAL"/>
                                  <C:comp name="VTODO"/>
                                </C:supported-calendar-component-set>
                                <D:supported-report-set>
                                  <D:supported-report>
                                    <D:report>
                                      <C:calendar-multiget xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                    </D:report>
                                  </D:supported-report>
                                  <D:supported-report>
                                    <D:report>
                                      <C:calendar-query xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                    </D:report>
                                  </D:supported-report>
                                </D:supported-report-set>
                                <CS:getctag xmlns:CS="http://calendarserver.org/ns/">157565ba8b0d3652b027c868d554f914</CS:getctag>
                                <D:current-user-principal>
                                  <D:href>/carldav/principals/users/${USER01}</D:href>
                                </D:current-user-principal>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request6)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response6))


        mockMvc.perform(options("/dav/{email}/", USER01))
                .andExpect(status().isOk())
                .andExpect(header().string("Allow", "OPTIONS, GET, HEAD, PROPFIND"))
                .andExpect(header().string("DAV", "1, 3, addressbook, calendar-access"))

        def request7 = """
                        <D:propfind xmlns:D="DAV:">
                          <D:prop>
                            <D:getcontenttype/>
                            <D:resourcetype/>
                            <D:getetag/>
                          </D:prop>
                        </D:propfind>
                        """

        def response7 = """
                        <D:multistatus xmlns:D="DAV:">
                          <D:response>
                            <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                            <D:propstat>
                              <D:prop>
                                <D:getcontenttype/>
                              </D:prop>
                              <D:status>HTTP/1.1 404 Not Found</D:status>
                            </D:propstat>
                            <D:propstat>
                              <D:prop>
                                <D:resourcetype>
                                  <D:collection/>
                                  <C:calendar xmlns:C="urn:ietf:params:xml:ns:caldav"/>
                                </D:resourcetype>
                                <D:getetag>"157565ba8b0d3652b027c868d554f914"</D:getetag>
                              </D:prop>
                              <D:status>HTTP/1.1 200 OK</D:status>
                            </D:propstat>
                          </D:response>
                        </D:multistatus>
                        """

        mockMvc.perform(propfind("/dav/{email}/calendar/", USER01)
                .contentType(TEXT_XML)
                .content(request7)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response7))
    }

    @Test
    void addVEvent() {
        def result1 = mockMvc.perform(put("/dav/{email}/calendar/cb13fd0d-da09-46bd-b113-247a355b2b98.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(ADD_VEVENT_REQUEST1)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def request2 = """\
                            <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                              <D:prop>
                                <D:getetag/>
                                <C:calendar-data/>
                              </D:prop>
                              <D:href>/carldav/dav/${USER01}/calendar/cb13fd0d-da09-46bd-b113-247a355b2b98.ics</D:href>
                            </C:calendar-multiget>
                       """

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/calendar/cb13fd0d-da09-46bd-b113-247a355b2b98.ics</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">
                                        BEGIN:VCALENDAR&#13;
                                        PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN&#13;
                                        VERSION:2.0&#13;
                                        BEGIN:VTIMEZONE&#13;
                                        TZID:Europe/Berlin&#13;
                                        BEGIN:DAYLIGHT&#13;
                                        TZOFFSETFROM:+0100&#13;
                                        TZOFFSETTO:+0200&#13;
                                        TZNAME:CEST&#13;
                                        DTSTART:19700329T020000&#13;
                                        RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU&#13;
                                        END:DAYLIGHT&#13;
                                        BEGIN:STANDARD&#13;
                                        TZOFFSETFROM:+0200&#13;
                                        TZOFFSETTO:+0100&#13;
                                        TZNAME:CET&#13;
                                        DTSTART:19701025T030000&#13;
                                        RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU&#13;
                                        END:STANDARD&#13;
                                        END:VTIMEZONE&#13;
                                        BEGIN:VEVENT&#13;
                                        CREATED:20200215T192814Z&#13;
                                        LAST-MODIFIED:20200215T192851Z&#13;
                                        DTSTAMP:20200215T192851Z&#13;
                                        UID:55d7d404-e3c5-4f73-843b-008a164d0b7d&#13;
                                        SUMMARY:test1&#13;
                                        ORGANIZER;RSVP=TRUE;PARTSTAT=ACCEPTED;ROLE=CHAIR:mailto:test1@localhost.de&#13;
                                        ATTENDEE;RSVP=TRUE;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT:mailto:test2@localhost.de&#13;
                                        DTSTART;TZID=Europe/Berlin:20190820T210000&#13;
                                        DTEND;TZID=Europe/Berlin:20190820T220000&#13;
                                        TRANSP:OPAQUE&#13;
                                        X-MOZ-SEND-INVITATIONS:FALSE&#13;
                                        END:VEVENT&#13;
                                        END:VCALENDAR&#13;
                                    </C:calendar-data>
                                    <D:getetag>${currentEtag}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                            """

        mockMvc.perform(report("/dav/{email}/calendar", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))
    }

    @Test
    void deleteVEvent() {
        addVEvent()

        mockMvc.perform(delete("/dav/{email}/calendar/cb13fd0d-da09-46bd-b113-247a355b2b98.ics", USER01, currentEtag)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andReturn()

        mockMvc.perform(get("/dav/{email}/calendar/cb13fd0d-da09-46bd-b113-247a355b2b98.ics", USER01, currentEtag))
                .andExpect(status().isNotFound())
    }

    @Test
    void addAndUpdateVEvent() {
        def veventRequest1 = ADD_VEVENT_REQUEST1.replace("DESCRIPTION:DESCRIPTION", "DESCRIPTION:DESCRIPTION update")

        addVEvent()

        def result1 = mockMvc.perform(put("/dav/{email}/calendar/cb13fd0d-da09-46bd-b113-247a355b2b98.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(veventRequest1)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andExpect(etag(not(currentEtag)))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def request2 = """\
                            <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                              <D:prop>
                                <D:getetag/>
                                <C:calendar-data/>
                              </D:prop>
                              <D:href>/carldav/dav/${USER01}/calendar/cb13fd0d-da09-46bd-b113-247a355b2b98.ics</D:href>
                            </C:calendar-multiget>
                       """

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/calendar/cb13fd0d-da09-46bd-b113-247a355b2b98.ics</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">
                                        BEGIN:VCALENDAR&#13;
                                        PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN&#13;
                                        VERSION:2.0&#13;
                                        BEGIN:VTIMEZONE&#13;
                                        TZID:Europe/Berlin&#13;
                                        BEGIN:DAYLIGHT&#13;
                                        TZOFFSETFROM:+0100&#13;
                                        TZOFFSETTO:+0200&#13;
                                        TZNAME:CEST&#13;
                                        DTSTART:19700329T020000&#13;
                                        RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU&#13;
                                        END:DAYLIGHT&#13;
                                        BEGIN:STANDARD&#13;
                                        TZOFFSETFROM:+0200&#13;
                                        TZOFFSETTO:+0100&#13;
                                        TZNAME:CET&#13;
                                        DTSTART:19701025T030000&#13;
                                        RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU&#13;
                                        END:STANDARD&#13;
                                        END:VTIMEZONE&#13;
                                        BEGIN:VEVENT&#13;
                                        CREATED:20200215T192814Z&#13;
                                        LAST-MODIFIED:20200215T192851Z&#13;
                                        DTSTAMP:20200215T192851Z&#13;
                                        UID:55d7d404-e3c5-4f73-843b-008a164d0b7d&#13;
                                        SUMMARY:test1&#13;
                                        ORGANIZER;RSVP=TRUE;PARTSTAT=ACCEPTED;ROLE=CHAIR:mailto:test1@localhost.de&#13;
                                        ATTENDEE;RSVP=TRUE;PARTSTAT=NEEDS-ACTION;ROLE=REQ-PARTICIPANT:mailto:test2@localhost.de&#13;
                                        DTSTART;TZID=Europe/Berlin:20190820T210000&#13;
                                        DTEND;TZID=Europe/Berlin:20190820T220000&#13;
                                        TRANSP:OPAQUE&#13;
                                        X-MOZ-SEND-INVITATIONS:FALSE&#13;
                                        END:VEVENT&#13;
                                        END:VCALENDAR&#13;
                                    </C:calendar-data>
                                    <D:getetag>${currentEtag}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                            """

        mockMvc.perform(report("/dav/{email}/calendar", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))
    }

    @Test
    void addVTodo() {
        def result1 = mockMvc.perform(put("/dav/{email}/calendar/8c7e686d-d84e-4926-8d15-7b474e76115f.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(ADD_VTODO_REQUEST1)
                .header("If-None-Match", "*"))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def request2 = """\
                           <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                             <D:prop>
                               <D:getetag/>
                               <C:calendar-data/>
                             </D:prop>
                             <D:href>/carldav/dav/${USER01}/calendar/8c7e686d-d84e-4926-8d15-7b474e76115f.ics</D:href>
                           </C:calendar-multiget>
                        """

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/calendar/8c7e686d-d84e-4926-8d15-7b474e76115f.ics</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">
                                      BEGIN:VCALENDAR&#13;
                                      PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN&#13;
                                      VERSION:2.0&#13;
                                      BEGIN:VTODO&#13;
                                      CREATED:20200215T194749Z&#13;
                                      LAST-MODIFIED:20200215T194754Z&#13;
                                      DTSTAMP:20200215T194754Z&#13;
                                      UID:8c7e686d-d84e-4926-8d15-7b474e76115f&#13;
                                      SUMMARY:todo1&#13;
                                      END:VTODO&#13;
                                      END:VCALENDAR&#13;
                                    </C:calendar-data>
                                    <D:getetag>${currentEtag}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response2))
    }

    @Test
    void deleteVTodo() {
        addVTodo()

        mockMvc.perform(delete("/dav/{email}/calendar/8c7e686d-d84e-4926-8d15-7b474e76115f.ics", USER01)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andReturn()

        mockMvc.perform(get("/dav/{email}/calendar/8c7e686d-d84e-4926-8d15-7b474e76115f.ics", USER01))
                .andExpect(status().isNotFound())
    }

    @Test
    void addAndUpdateVTodo() {
        addVTodo()

        def vtodoRequest1 = ADD_VTODO_REQUEST1.replace("test1", "test2")

        def result1 = mockMvc.perform(put("/dav/{email}/calendar/8c7e686d-d84e-4926-8d15-7b474e76115f.ics", USER01)
                .contentType(TEXT_CALENDAR)
                .content(vtodoRequest1)
                .header("If-Match", currentEtag))
                .andExpect(status().isNoContent())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def request2 = """\
                           <C:calendar-multiget xmlns:D="DAV:" xmlns:C="urn:ietf:params:xml:ns:caldav">
                             <D:prop>
                               <D:getetag/>
                               <C:calendar-data/>
                             </D:prop>
                             <D:href>/carldav/dav/${USER01}/calendar/8c7e686d-d84e-4926-8d15-7b474e76115f.ics</D:href>
                           </C:calendar-multiget>
                        """

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/calendar/8c7e686d-d84e-4926-8d15-7b474e76115f.ics</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <C:calendar-data xmlns:C="urn:ietf:params:xml:ns:caldav" C:content-type="text/calendar" C:version="2.0">
                                      BEGIN:VCALENDAR&#13;
                                      PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN&#13;
                                      VERSION:2.0&#13;
                                      BEGIN:VTODO&#13;
                                      CREATED:20200215T194749Z&#13;
                                      LAST-MODIFIED:20200215T194754Z&#13;
                                      DTSTAMP:20200215T194754Z&#13;
                                      UID:8c7e686d-d84e-4926-8d15-7b474e76115f&#13;
                                      SUMMARY:todo1&#13;
                                      END:VTODO&#13;
                                      END:VCALENDAR&#13;
                                    </C:calendar-data>
                                    <D:getetag>${currentEtag}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(report("/dav/{email}/calendar/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response2))
    }

    @Test
    void fetchEmptyAddressbookFirstTimeFromCalDavResource() {
        def request1 = """\
                           <d:propfind xmlns:d="DAV:">
                             <d:prop>
                               <d:current-user-principal/>
                             </d:prop>
                           </d:propfind>
                        """

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/calendar/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:current-user-principal>
                                      <D:href>/carldav/principals/users/${USER01}</D:href>
                                    </D:current-user-principal>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/dav/{email}/calendar", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

        def request2 = """\
                           <d:propfind xmlns:d="DAV:" xmlns:card="urn:ietf:params:xml:ns:carddav" xmlns:cs="http://calendarserver.org/ns/">
                             <d:prop>
                               <card:addressbook-home-set/>
                               <d:group-membership/>
                             </d:prop>
                           </d:propfind>
                        """

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/principals/users/${USER01}</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <d:group-membership xmlns:d="DAV:"/>
                                  </D:prop>
                                  <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                                <D:propstat>
                                  <D:prop>
                                    <CARD:addressbook-home-set xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                                      <D:href>/carldav/dav/${USER01}/contacts</D:href>
                                    </CARD:addressbook-home-set>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/principals/users/{email}", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))

        def request3 = """\
                           <d:propfind xmlns:d="DAV:">
                             <d:prop>
                               <d:current-user-privilege-set/>
                               <d:resourcetype/>
                               <d:displayname/>
                             </d:prop>
                           </d:propfind>
                        """

        def response3 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:resourcetype>
                                      <D:collection/>
                                      <CARD:addressbook xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
                                    </D:resourcetype>
                                    <D:displayname>contactDisplayName</D:displayname>
                                    <D:current-user-privilege-set>
                                      <D:privilege>
                                        <D:read/>
                                      </D:privilege>
                                      <D:privilege>
                                        <D:write/>
                                      </D:privilege>
                                    </D:current-user-privilege-set>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request3)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response3))

        def request4 = """\
                           <d:propfind xmlns:d="DAV:" xmlns:cs="http://calendarserver.org/ns/">
                             <d:prop>
                               <cs:getctag/>
                               <d:sync-token/>
                             </d:prop>
                           </d:propfind>
                        """

        def response4 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <cs:getctag xmlns:cs="http://calendarserver.org/ns/"/>
                                    <d:sync-token xmlns:d="DAV:"/>
                                  </D:prop>
                                  <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request4)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response4))

        def request5 = """\
                           <d:propfind xmlns:d="DAV:">
                             <d:prop>
                               <d:getetag/>
                             </d:prop>
                           </d:propfind>
                        """

        def response5 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>"6ae40e84e2caf8e8fea53ce5396de66f"</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request5)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response5))
    }

    @Test
    void fetchEmptyAddressbookFirstTimeFromCardDavResource() {
        def request1 = """\
                           <d:propfind xmlns:d="DAV:">
                             <d:prop>
                               <d:current-user-principal/>
                             </d:prop>
                           </d:propfind>
                        """

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:current-user-principal>
                                      <D:href>/carldav/principals/users/${USER01}</D:href>
                                    </D:current-user-principal>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response1))

        def request2 = """\
                           <d:propfind xmlns:d="DAV:" xmlns:card="urn:ietf:params:xml:ns:carddav" xmlns:cs="http://calendarserver.org/ns/">
                             <d:prop>
                               <card:addressbook-home-set/>
                               <d:group-membership/>
                             </d:prop>
                           </d:propfind>
                        """

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/principals/users/${USER01}</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <d:group-membership xmlns:d="DAV:"/>
                                  </D:prop>
                                  <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                                <D:propstat>
                                  <D:prop>
                                    <CARD:addressbook-home-set xmlns:CARD="urn:ietf:params:xml:ns:carddav">
                                      <D:href>/carldav/dav/${USER01}/contacts</D:href>
                                    </CARD:addressbook-home-set>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/principals/users/{email}", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response2))

        def request3 = """\
                           <d:propfind xmlns:d="DAV:">
                             <d:prop>
                               <d:current-user-privilege-set/>
                               <d:resourcetype/>
                               <d:displayname/>
                             </d:prop>
                           </d:propfind>
                        """

        def response3 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:resourcetype>
                                      <D:collection/>
                                      <CARD:addressbook xmlns:CARD="urn:ietf:params:xml:ns:carddav"/>
                                    </D:resourcetype>
                                    <D:displayname>contactDisplayName</D:displayname>
                                    <D:current-user-privilege-set>
                                      <D:privilege>
                                        <D:read/>
                                      </D:privilege>
                                      <D:privilege>
                                        <D:write/>
                                      </D:privilege>
                                    </D:current-user-privilege-set>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request3)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response3))

        def request4 = """\
                           <d:propfind xmlns:d="DAV:" xmlns:cs="http://calendarserver.org/ns/">
                             <d:prop>
                               <cs:getctag/>
                               <d:sync-token/>
                             </d:prop>
                           </d:propfind>
                        """

        def response4 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <cs:getctag xmlns:cs="http://calendarserver.org/ns/"/>
                                    <d:sync-token xmlns:d="DAV:"/>
                                  </D:prop>
                                  <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request4)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response4))

        def request5 = """\
                           <d:propfind xmlns:d="DAV:">
                             <d:prop>
                               <d:getetag/>
                             </d:prop>
                           </d:propfind>
                        """

        def response5 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>"6ae40e84e2caf8e8fea53ce5396de66f"</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request5)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response5))
    }

    @Test
    void addVCard() {
        def result1 = mockMvc.perform(put("/dav/{email}/contacts/80f58bbd-53bb-42bf-9fd7-c81f20601dfa.vcf", USER01)
                .contentType(TEXT_VCARD)
                .content(ADD_VCARD_REQUEST1))
                .andExpect(status().isCreated())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result1.getResponse().getHeader(ETAG)

        def request1 = """\
                           <d:propfind xmlns:d="DAV:" xmlns:cs="http://calendarserver.org/ns/">
                             <d:prop>
                               <cs:getctag/>
                               <d:sync-token/>
                             </d:prop>
                           </d:propfind>
                       """

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <cs:getctag xmlns:cs="http://calendarserver.org/ns/"/>
                                    <d:sync-token xmlns:d="DAV:"/>
                                  </D:prop>
                                  <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/dav/{email}/contacts/", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response1))

        def request2 = """\
                           <d:propfind xmlns:d="DAV:">
                             <d:prop>
                               <d:getetag/>
                             </d:prop>
                           </d:propfind>
                        """

        def result2 = mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def collectionEtag = getetag(result2, 0)
        def itemEtag = getetag(result2, 1)

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${collectionEtag}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/80f58bbd-53bb-42bf-9fd7-c81f20601dfa.vcf</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${itemEtag}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        assertThat(result2, equalXml(response2))

        def request3 = """\
                           <card:addressbook-multiget xmlns:d="DAV:" xmlns:card="urn:ietf:params:xml:ns:carddav">
                             <d:prop>
                               <d:getetag/>
                               <card:address-data/>
                             </d:prop>
                             <d:href>/carldav/dav/test01@localhost.de/contacts/80f58bbd-53bb-42bf-9fd7-c81f20601dfa.vcf</d:href>
                            </card:addressbook-multiget>
                         """

        def response3 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/80f58bbd-53bb-42bf-9fd7-c81f20601dfa.vcf</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <card:address-data xmlns:card="urn:ietf:params:xml:ns:carddav"/>
                                  </D:prop>
                                  <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${itemEtag}</D:getetag>
                                    <CARD:address-data xmlns:CARD="urn:ietf:params:xml:ns:carddav">BEGIN:VCARD&#13;
                                      FN:test1&#13;
                                      N:;test1;;;&#13;
                                      UID:28a55da8-6de7-469c-8dcd-7bc88c9ec1da&#13;
                                      VERSION:3.0&#13;
                                      END:VCARD
                                    </CARD:address-data>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                            """

        mockMvc.perform(report("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request3)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response3))
    }

    @Test
    void deleteVCard() {
        addVCard()

        def request1 = """\
                           <d:propfind xmlns:d="DAV:">
                             <d:prop>
                               <d:getetag/>
                             </d:prop>
                           </d:propfind>
                        """

        def result1 = mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def collectionEtag = getetag(result1, 0)
        def itemEtag = getetag(result1, 1)

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${collectionEtag}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/80f58bbd-53bb-42bf-9fd7-c81f20601dfa.vcf</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${itemEtag}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        assertThat(result1, equalXml(response1))

        mockMvc.perform(delete("/dav/{email}/contacts/80f58bbd-53bb-42bf-9fd7-c81f20601dfa.vcf", USER01))
                .andExpect(status().isNoContent())

        def request2 = """\
                           <d:propfind xmlns:d="DAV:" xmlns:cs="http://calendarserver.org/ns/">
                             <d:prop>
                               <cs:getctag/>
                               <d:sync-token/>
                             </d:prop>
                           </d:propfind>
                       """

        def response2 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <cs:getctag xmlns:cs="http://calendarserver.org/ns/"/>
                                    <d:sync-token xmlns:d="DAV:"/>
                                  </D:prop>
                                  <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/dav/{email}/contacts/", USER01)
                .contentType(APPLICATION_XML)
                .content(request2)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response2))

        def request3 = """\
                           <d:propfind xmlns:d="DAV:">
                             <d:prop>
                               <d:getetag/>
                             </d:prop>
                           </d:propfind>
                        """

        def result3 = mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request3)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def collectionEtag3 = getetag(result3, 0)

        def response3 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${collectionEtag3}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        assertThat(result3, equalXml(response3))
    }

    @Test
    void addAndUpdateVCard() {
        addVCard()

        def request1 = """\
                           <d:propfind xmlns:d="DAV:">
                             <d:prop>
                               <d:getetag/>
                             </d:prop>
                           </d:propfind>
                        """

        def result1 = mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request1)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def collectionEtag = getetag(result1, 0)
        def itemEtag = getetag(result1, 1)

        def response1 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${collectionEtag}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/80f58bbd-53bb-42bf-9fd7-c81f20601dfa.vcf</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${itemEtag}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        assertThat(result1, equalXml(response1))

        def request2 = ADD_VCARD_REQUEST1.replace("N:;test1;;;", "N:;test2;;;")

        def result2 = mockMvc.perform(put("/dav/{email}/contacts/80f58bbd-53bb-42bf-9fd7-c81f20601dfa.vcf", USER01)
                .contentType(TEXT_VCARD)
                .content(request2))
                .andExpect(status().isNoContent())
                .andExpect(etag(notNullValue()))
                .andReturn()

        currentEtag = result2.getResponse().getHeader(ETAG)

        def request3 = """\
                           <d:propfind xmlns:d="DAV:" xmlns:cs="http://calendarserver.org/ns/">
                             <d:prop>
                               <cs:getctag/>
                               <d:sync-token/>
                             </d:prop>
                           </d:propfind>
                       """.stripIndent()

        def response3 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <cs:getctag xmlns:cs="http://calendarserver.org/ns/"/>
                                    <d:sync-token xmlns:d="DAV:"/>
                                  </D:prop>
                                  <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        mockMvc.perform(propfind("/dav/{email}/contacts/", USER01)
                .contentType(APPLICATION_XML)
                .content(request3)
                .header("depth", "0"))
                .andExpect(status().isMultiStatus())
                .andExpect(xml(response3))

        def request4 = """\
                           <d:propfind xmlns:d="DAV:">
                             <d:prop>
                               <d:getetag/>
                             </d:prop>
                           </d:propfind>
                        """

        def result4 = mockMvc.perform(propfind("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request4)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andReturn().getResponse().getContentAsString()

        def collectionEtag4 = getetag(result4, 0)
        def itemEtag4 = getetag(result4, 1)

        def response4 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${collectionEtag4}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/80f58bbd-53bb-42bf-9fd7-c81f20601dfa.vcf</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${itemEtag4}</D:getetag>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                         """

        assertThat(result4, equalXml(response4))

        def request5 = """\
                           <card:addressbook-multiget xmlns:d="DAV:" xmlns:card="urn:ietf:params:xml:ns:carddav">
                             <d:prop>
                               <d:getetag/>
                               <card:address-data/>
                             </d:prop>
                             <d:href>/carldav/dav/test01@localhost.de/contacts/80f58bbd-53bb-42bf-9fd7-c81f20601dfa.vcf</d:href>
                            </card:addressbook-multiget>
                         """

        def response5 = """\
                            <D:multistatus xmlns:D="DAV:">
                              <D:response>
                                <D:href>/carldav/dav/${USER01}/contacts/80f58bbd-53bb-42bf-9fd7-c81f20601dfa.vcf</D:href>
                                <D:propstat>
                                  <D:prop>
                                    <card:address-data xmlns:card="urn:ietf:params:xml:ns:carddav"/>
                                  </D:prop>
                                  <D:status>HTTP/1.1 404 Not Found</D:status>
                                </D:propstat>
                                <D:propstat>
                                  <D:prop>
                                    <D:getetag>${itemEtag4}</D:getetag>
                                    <CARD:address-data xmlns:CARD="urn:ietf:params:xml:ns:carddav">BEGIN:VCARD&#13;
                                      FN:test1&#13;
                                      N:;test2;;;&#13;
                                      UID:28a55da8-6de7-469c-8dcd-7bc88c9ec1da&#13;
                                      VERSION:3.0&#13;
                                      END:VCARD
                                    </CARD:address-data>
                                  </D:prop>
                                  <D:status>HTTP/1.1 200 OK</D:status>
                                </D:propstat>
                              </D:response>
                            </D:multistatus>
                            """

        mockMvc.perform(report("/dav/{email}/contacts", USER01)
                .contentType(APPLICATION_XML)
                .content(request5)
                .header("depth", "1"))
                .andExpect(status().isMultiStatus())
                .andExpect(textXmlContentType())
                .andExpect(xml(response5))
    }
}

