package testutil.builder;

/**
 * @author Kamill Sokol
 */
public class GeneralResponse {
    private GeneralResponse() {
        //private
    }

    public static String INTERNAL_SERVER_ERROR = """\
                                                    <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                                        <cosmo:internal-server-error></cosmo:internal-server-error>
                                                    </D:error>"""

    public static String RESOURCE_MUST_BE_NULL = """\
                                                    <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                                        <D:resource-must-be-null>A resource exists at the request URI</D:resource-must-be-null>
                                                    </D:error>"""

    public static String NOT_FOUND = """\
                                        <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                            <cosmo:not-found></cosmo:not-found>
                                        </D:error>"""

    public static String NOT_SUPPORTED_PRIVILEGE = """\
                                                       <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                                            <D:not-supported-privilege>No unprotected ACEs are supported on this resource</D:not-supported-privilege>
                                                       </D:error>"""

    public static String UNPROCESSABLE_ENTITY = '''\
                                                <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                                    <cosmo:unprocessable-entity>Expected either {DAV:}self or {DAV:}principal-property child of {DAV:}principal-match</cosmo:unprocessable-entity>
                                                </D:error>'''

    public static String CONFLICT_RESPONSE = """\
                                                <D:error xmlns:cosmo="http://osafoundation.org/cosmo/DAV" xmlns:D="DAV:">
                                                    <cosmo:conflict>One or more intermediate collections must be created</cosmo:conflict>
                                                </D:error>"""
}
