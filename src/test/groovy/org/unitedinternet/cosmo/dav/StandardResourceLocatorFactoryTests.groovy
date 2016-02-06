package org.unitedinternet.cosmo.dav

import org.junit.Test

/**
 * @author Kamill Sokol
 */
class StandardResourceLocatorFactoryTests {

    def uut = new StandardResourceLocatorFactory()

    @Test
    void "create calendar resource locator by URI"() {
        def contextPath = "/dav"
        def path = "/test01@localhost.de/calendar/BC9458C9-C221-4E23-BA24-1E3D4EDBE65B.ics"
        def locator = createResourceLocatorByUri(contextPath, path)

        assert locator.getPath() == path
    }

    @Test
    void "create resource locator by URI"() {
        def contextPath = "/dav"
        def path = "/test01@localhost.de"
        def locator = createResourceLocatorByUri(contextPath, path)

        assert locator.getPath() == path
    }

    @Test
    void "create url encoded resource locator by URI"() {
        def contextPath = "/dav"
        def pathEncoded = "/test01%40localhost.de"
        def pathDecoded = "/test01@localhost.de"
        def locator = createResourceLocatorByUri(contextPath, pathEncoded)

        assert locator.getPath() == pathDecoded
    }

    @Test
    void "create user collection principal resource locator by URI"() {
        def contextPath = "/principals"
        def path = "/users"
        def locator = createResourceLocatorByUri(contextPath, path)

        assert locator.getPath() == path
    }

    @Test
    void "create user principal resource locator by URI"() {
        def contextPath = "/principals"
        def path = "/users/test01@localhost.de"
        def locator = createResourceLocatorByUri(contextPath, path)

        assert locator.getPath() == path
    }

    @Test
    void "create user principal root resource locator by URI"() {
        def contextPath = "/principals"
        def path = ""
        def locator = createResourceLocatorByUri(contextPath, path)

        assert locator.getPath() == "/"
    }

    def createResourceLocatorByUri(String contextPath, String path) {
        def context = new URL("http", "localhost", 80, contextPath)
        return uut.createResourceLocatorByUri(context, contextPath + path)
    }
}