package dav

import org.junit.Test
import org.springframework.security.test.context.support.WithUserDetails
import org.unitedinternet.cosmo.IntegrationTestSupport

import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static testutil.TestUser.USER01
import static testutil.TestUser.USER02
import static testutil.builder.GeneralResponse.PRECONDITION_FAILED_RESPONSE
import static testutil.mockmvc.CustomResultMatchers.*

/**
 * @author Kamill Sokol
 */
class EtagTests extends IntegrationTestSupport {

    @WithUserDetails(USER01)
    @Test
    public void user01IfMatchWildcardIsOkReturnEtag() throws Exception {
        mockMvc.perform(head("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .header("If-Match", '*'))
                .andExpect(status().isOk())
                .andExpect(etag(is('"ghFexXxxU+9KC/of1jmJ82wMFig="')));
    }

    @WithUserDetails(USER01)
    @Test
    public void user01IfMatchEtagIsOkReturnEtag() throws Exception {
        mockMvc.perform(head("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .header("If-Match", '"ghFexXxxU+9KC/of1jmJ82wMFig="'))
                .andExpect(status().isOk())
                .andExpect(etag(is('"ghFexXxxU+9KC/of1jmJ82wMFig="')));
    }

    @WithUserDetails(USER02)
    @Test
    public void user02IfMatchEtagIsOkReturnLastModified() throws Exception {
        mockMvc.perform(head("/dav/collection/{uid}", "1e359448-1ee0-4151-872d-eea0ee462bc6")
                .header("If-Match", '"ghFexXxxU+9KC/of1jmJ82wMFig="'))
                .andExpect(status().isOk())
                .andExpect(lastModified(notNullValue()))
    }

    @WithUserDetails(USER02)
    @Test
    public void user02NoEtagIsOkReturnLastModified() throws Exception {
        mockMvc.perform(head("/dav/collection/{uid}", "1e359448-1ee0-4151-872d-eea0ee462bc6"))
                .andExpect(status().isOk())
                .andExpect(lastModified(notNullValue()))
    }

    @WithUserDetails(USER01)
    @Test
    public void user01IfNoneMatchGetIsNotModified() throws Exception {
        mockMvc.perform(get("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .header("If-None-Match", '"ghFexXxxU+9KC/of1jmJ82wMFig="'))
                .andExpect(status().isNotModified())
    }

    @WithUserDetails(USER01)
    @Test
    public void user01IfNoneMatchOptionsIsPrecodnitionFailed() throws Exception {
        mockMvc.perform(options("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .header("If-None-Match", '"ghFexXxxU+9KC/of1jmJ82wMFig="'))
                .andExpect(status().isPreconditionFailed())
                .andExpect(xml(PRECONDITION_FAILED_RESPONSE))
    }

    @WithUserDetails(USER01)
    @Test
    public void user01IfNoneMatchGetIsOk() throws Exception {
        mockMvc.perform(get("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .header("If-None-Match", '"1hFexXxxU+9KC/of1jmJ82wMFig="'))
                .andExpect(status().isOk())
    }

    @WithUserDetails(USER01)
    @Test
    public void user01IfNoneMatchHeadIsNotModified() throws Exception {
        mockMvc.perform(head("/dav/collection/{uid}", "de359448-1ee0-4151-872d-eea0ee462bc6")
                .header("If-None-Match", '"ghFexXxxU+9KC/of1jmJ82wMFig="'))
                .andExpect(status().isNotModified())
    }

    @WithUserDetails(USER02)
    @Test
    public void user02IfNoneMatchOptionsIsOk() throws Exception {
        mockMvc.perform(options("/dav/collection/{uid}", "1e359448-1ee0-4151-872d-eea0ee462bc6")
                .header("If-None-Match", '"1hFexXxxU+9KC/of1jmJ82wMFig="'))
                .andExpect(status().isOk())
    }
}
