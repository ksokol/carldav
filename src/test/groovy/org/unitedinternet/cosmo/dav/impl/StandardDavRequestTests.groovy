package org.unitedinternet.cosmo.dav.impl

import org.junit.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.unitedinternet.cosmo.dav.DavResourceLocatorFactory
import org.unitedinternet.cosmo.dav.StandardResourceLocator

import javax.servlet.http.HttpServletRequest

import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.*

/**
 * @author Kamill Sokol
 */
class StandardDavRequestTests {

    @Test
    void "principalsUri"() {
        HttpServletRequest httpRequest = new MockHttpServletRequest()
        httpRequest.setRequestURI("/principals/users")

        def DavResourceLocatorFactory locatorFactory = mock(DavResourceLocatorFactory)

        when(locatorFactory.createResourceLocatorByUri(any(URL.class), anyString())).thenReturn(mock(StandardResourceLocator))

        def davRequest = new StandardDavRequest(httpRequest, locatorFactory)

        davRequest.getResourceLocator()

        verify(locatorFactory).createResourceLocatorByUri(new URL("http://localhost:80/principals"), "/principals/users")
    }

    @Test
    void "davUri"() {
        HttpServletRequest httpRequest = new MockHttpServletRequest()
        httpRequest.setRequestURI("/dav/test01@localhost.de/calendar")

        def DavResourceLocatorFactory locatorFactory = mock(DavResourceLocatorFactory)

        when(locatorFactory.createResourceLocatorByUri(any(URL.class), anyString())).thenReturn(mock(StandardResourceLocator))

        def davRequest = new StandardDavRequest(httpRequest, locatorFactory)

        davRequest.getResourceLocator()

        verify(locatorFactory).createResourceLocatorByUri(new URL("http://localhost:80/dav"), "/dav/test01@localhost.de/calendar")
    }
}
