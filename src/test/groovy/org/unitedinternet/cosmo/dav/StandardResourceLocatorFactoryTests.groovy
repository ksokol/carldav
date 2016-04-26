package org.unitedinternet.cosmo.dav

import org.junit.Test
import org.springframework.mock.web.MockHttpServletRequest

import javax.servlet.http.HttpServletRequest

/**
 * @author Kamill Sokol
 */
class StandardResourceLocatorFactoryTests {

    @Test
    void "create calendar resource locator by URI"() {
        def contextPath = "/contextPath"
        def path = "/dav/test01@localhost.de/calendar/BC9458C9-C221-4E23-BA24-1E3D4EDBE65B.ics"
        def context = createContext(contextPath)
        def locator = new StandardResourceLocatorFactory(contextPath).createResourceLocatorByUri(context, contextPath + path)

        assert locator.getPath() == path
        assert locator.getBaseHref() == contextPath
    }

    @Test
    void "create resource locator by URI"() {
        def contextPath = "/contextPath"
        def path = "/dav/test01@localhost.de"
        def context = createContext(contextPath)
        def locator = new StandardResourceLocatorFactory(contextPath).createResourceLocatorByUri(context, contextPath + path)

        assert locator.getPath() == path
        assert locator.getBaseHref() == contextPath
    }

    @Test
    void "create url encoded resource locator by URI"() {
        def contextPath = "/contextPath"
        def pathEncoded = "/dav/test01%40localhost.de"
        def pathDecoded = "/dav/test01@localhost.de"
        def context = createContext(contextPath)
        def locator = new StandardResourceLocatorFactory(contextPath).createResourceLocatorByUri(context, contextPath + pathEncoded)

        assert locator.getPath() == pathDecoded
        assert locator.getBaseHref() == contextPath
    }

    @Test
    void "create user collection principal resource locator by URI"() {
        def contextPath = "/contextPath"
        def path = "/principals/users"
        def context = createContext(contextPath)
        def locator = new StandardResourceLocatorFactory(contextPath).createResourceLocatorByUri(context, contextPath + path)

        assert locator.getPath() == path
        assert locator.getBaseHref() == contextPath
    }

    @Test
    void "create user principal resource locator by URI"() {
        def contextPath = "/contextPath"
        def path = "/principals/users/test01@localhost.de"
        def context = createContext(contextPath)
        def locator = new StandardResourceLocatorFactory(contextPath).createResourceLocatorByUri(context, contextPath + path)

        assert locator.getPath() == path
        assert locator.getBaseHref() == contextPath
    }

    @Test
    void "create user principal root resource locator by URI"() {
        def contextPath = "/contextPath"
        def path = ""
        def context = createContext(contextPath)
        def locator = new StandardResourceLocatorFactory(contextPath).createResourceLocatorByUri(context, contextPath + path)

        assert locator.getPath() == ""
        assert locator.getBaseHref() == contextPath
    }

    @Test
    void "principalsUri"() {
        HttpServletRequest httpRequest = new MockHttpServletRequest()
        httpRequest.setRequestURI("/principals/users")

        def contextPath = "/"
        def locator = new StandardResourceLocatorFactory(contextPath).createResourceLocatorFromRequest(httpRequest)

        assert locator.getPath() == "/principals/users"
        assert locator.getBaseHref() == "/"
    }

    @Test
    void "davUri"() {
        HttpServletRequest httpRequest = new MockHttpServletRequest()
        httpRequest.setRequestURI("/dav/test01@localhost.de/calendar")

        def locator = new StandardResourceLocatorFactory("/").createResourceLocatorFromRequest(httpRequest)

        assert locator.getPath() == "/dav/test01@localhost.de/calendar"
        assert locator.getBaseHref() == "/"
    }

    def createContext(String contextPath) {
        return new URL("http", "localhost", 80, contextPath)
    }
}
