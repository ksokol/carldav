package carldav.security

import org.junit.Before
import org.junit.Test
import org.springframework.mock.web.MockHttpServletRequest

/**
 * @author Kamill Sokol
 */
class NamedGroupRegexRequestMatcherTest {

    NamedGroupRegexRequestMatcher matcher
    MockHttpServletRequest request

    @Before
    void before() {
        request = new MockHttpServletRequest()
        request.setServletPath('')
    }

    @Test
    void shouldMatchUri() {
        matcher = new NamedGroupRegexRequestMatcher('/test/uri/.*')
        request.setPathInfo('/test/uri/path')

        assert matcher.matches(request)
    }

    @Test
    void shouldNotMatchUri() {
        matcher = new NamedGroupRegexRequestMatcher('/test/uri/.*')
        request.setPathInfo('/other/uri')

        assert !matcher.matches(request)
    }

    @Test
    void shouldExtractUriTemplateVariables() {
        matcher = new NamedGroupRegexRequestMatcher('/test/(?<el1>[a-z]*)/(?<el2>[0-9]*)/.*')
        request.setPathInfo('/test/abc/123/other')

        assert matcher.extractUriTemplateVariables(request) == [el1: 'abc', el2: '123']
    }

    @Test
    void shouldReturnEmptyMapWhenNoUriTemplateVariablesFound() {
        matcher = new NamedGroupRegexRequestMatcher('/test/(?<el1>[a-z]*)/(?<el2>[0-9]*)/.*')
        request.setPathInfo('/test/abc')

        assert matcher.extractUriTemplateVariables(request) == [:]
    }
}
